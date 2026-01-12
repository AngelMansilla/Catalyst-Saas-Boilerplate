-- =====================================================
-- Catalyst Payment Service - Initial Schema
-- Version: 1.0
-- Description: Creates tables for subscriptions, invoices, payments, and customers
-- =====================================================

-- =====================================================
-- CUSTOMERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    stripe_customer_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT customers_email_check CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
);

CREATE INDEX idx_customers_user_id ON payment.customers(user_id);
CREATE INDEX idx_customers_stripe_customer_id ON payment.customers(stripe_customer_id);

-- =====================================================
-- SUBSCRIPTIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES payment.customers(id) ON DELETE CASCADE,
    stripe_subscription_id VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    billing_cycle VARCHAR(20),
    trial_end_date TIMESTAMP,
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    canceled_at TIMESTAMP,
    cancellation_reason VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT subscriptions_status_check CHECK (status IN ('TRIAL', 'ACTIVE', 'PAST_DUE', 'CANCELED', 'EXPIRED')),
    CONSTRAINT subscriptions_tier_check CHECK (tier IN ('FREE_TRIAL', 'PROFESSIONAL', 'CLINIC')),
    CONSTRAINT subscriptions_billing_cycle_check CHECK (billing_cycle IN ('MONTHLY', 'ANNUAL'))
);

CREATE INDEX idx_subscriptions_customer_id ON payment.subscriptions(customer_id);
CREATE INDEX idx_subscriptions_stripe_subscription_id ON payment.subscriptions(stripe_subscription_id);
CREATE INDEX idx_subscriptions_status ON payment.subscriptions(status);
CREATE INDEX idx_subscriptions_trial_end_date ON payment.subscriptions(trial_end_date) WHERE trial_end_date IS NOT NULL;

-- =====================================================
-- INVOICES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES payment.subscriptions(id) ON DELETE CASCADE,
    stripe_invoice_id VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL,
    amount_due DECIMAL(10, 2) NOT NULL,
    amount_paid DECIMAL(10, 2) DEFAULT 0,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    invoice_pdf_url TEXT,
    hosted_invoice_url TEXT,
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT invoices_status_check CHECK (status IN ('DRAFT', 'OPEN', 'PAID', 'VOID', 'UNCOLLECTIBLE')),
    CONSTRAINT invoices_amount_check CHECK (amount_due >= 0 AND amount_paid >= 0)
);

CREATE INDEX idx_invoices_subscription_id ON payment.invoices(subscription_id);
CREATE INDEX idx_invoices_stripe_invoice_id ON payment.invoices(stripe_invoice_id);
CREATE INDEX idx_invoices_status ON payment.invoices(status);
CREATE INDEX idx_invoices_due_date ON payment.invoices(due_date) WHERE due_date IS NOT NULL;

-- =====================================================
-- PAYMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL REFERENCES payment.invoices(id) ON DELETE CASCADE,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    status VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method_type VARCHAR(50),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT payments_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELED')),
    CONSTRAINT payments_amount_check CHECK (amount >= 0)
);

CREATE INDEX idx_payments_invoice_id ON payment.payments(invoice_id);
CREATE INDEX idx_payments_stripe_payment_intent_id ON payment.payments(stripe_payment_intent_id);
CREATE INDEX idx_payments_status ON payment.payments(status);

-- =====================================================
-- WEBHOOK EVENTS TABLE (Idempotency)
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stripe_event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payload JSONB NOT NULL,
    
    CONSTRAINT webhook_events_stripe_event_id_check CHECK (stripe_event_id <> '')
);

CREATE INDEX idx_webhook_events_stripe_event_id ON payment.webhook_events(stripe_event_id);
CREATE INDEX idx_webhook_events_event_type ON payment.webhook_events(event_type);
CREATE INDEX idx_webhook_events_processed_at ON payment.webhook_events(processed_at);

-- =====================================================
-- PROCESSED EVENTS TABLE (Kafka Idempotency)
-- =====================================================
CREATE TABLE IF NOT EXISTS payment.processed_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT processed_events_event_id_check CHECK (event_id IS NOT NULL)
);

CREATE INDEX idx_processed_events_event_id ON payment.processed_events(event_id);
CREATE INDEX idx_processed_events_processed_at ON payment.processed_events(processed_at);

-- =====================================================
-- UPDATED_AT TRIGGER FUNCTION
-- =====================================================
CREATE OR REPLACE FUNCTION payment.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- APPLY UPDATED_AT TRIGGERS
-- =====================================================
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON payment.customers
    FOR EACH ROW
    EXECUTE FUNCTION payment.update_updated_at_column();

CREATE TRIGGER update_subscriptions_updated_at
    BEFORE UPDATE ON payment.subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION payment.update_updated_at_column();

CREATE TRIGGER update_invoices_updated_at
    BEFORE UPDATE ON payment.invoices
    FOR EACH ROW
    EXECUTE FUNCTION payment.update_updated_at_column();

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payment.payments
    FOR EACH ROW
    EXECUTE FUNCTION payment.update_updated_at_column();

-- =====================================================
-- COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON TABLE payment.customers IS 'Stores customer information linked to Stripe customers';
COMMENT ON TABLE payment.subscriptions IS 'Subscription lifecycle with state machine (TRIAL → ACTIVE → PAST_DUE → CANCELED/EXPIRED)';
COMMENT ON TABLE payment.invoices IS 'Invoice records from Stripe';
COMMENT ON TABLE payment.payments IS 'Payment transaction records';
COMMENT ON TABLE payment.webhook_events IS 'Stripe webhook events for idempotency';
COMMENT ON TABLE payment.processed_events IS 'Kafka event IDs for consumer idempotency';

