-- Catalyst Database Initialization Script
-- This script runs automatically when PostgreSQL container starts for the first time

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schemas for different services
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS audit;

-- Set search path
ALTER DATABASE catalyst_db SET search_path TO public, payment, auth, audit;

-- Create audit table for tracking changes
CREATE TABLE IF NOT EXISTS audit.event_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    event_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- Create index for audit queries
CREATE INDEX IF NOT EXISTS idx_event_log_entity ON audit.event_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_event_log_created_at ON audit.event_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_event_log_user_id ON audit.event_log(user_id);

-- Create users table in auth schema
CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    name VARCHAR(255),
    image_url TEXT,
    provider VARCHAR(50),
    provider_account_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Create indexes for auth.users
CREATE INDEX IF NOT EXISTS idx_users_email ON auth.users(email);
CREATE INDEX IF NOT EXISTS idx_users_provider ON auth.users(provider, provider_account_id);

-- Create sessions table for JWT refresh tokens
CREATE TABLE IF NOT EXISTS auth.sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    refresh_token TEXT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create index for sessions
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON auth.sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON auth.sessions(expires_at);

-- Create payment customers table
CREATE TABLE IF NOT EXISTS payment.customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    stripe_customer_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create index for payment.customers
CREATE INDEX IF NOT EXISTS idx_customers_user_id ON payment.customers(user_id);
CREATE INDEX IF NOT EXISTS idx_customers_stripe_id ON payment.customers(stripe_customer_id);

-- Create payment subscriptions table
CREATE TABLE IF NOT EXISTS payment.subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES payment.customers(id) ON DELETE CASCADE,
    stripe_subscription_id VARCHAR(255) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    plan_id VARCHAR(255) NOT NULL,
    current_period_start TIMESTAMP WITH TIME ZONE,
    current_period_end TIMESTAMP WITH TIME ZONE,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payment.subscriptions
CREATE INDEX IF NOT EXISTS idx_subscriptions_customer_id ON payment.subscriptions(customer_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_stripe_id ON payment.subscriptions(stripe_subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON payment.subscriptions(status);

-- Create payment transactions table
CREATE TABLE IF NOT EXISTS payment.transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES payment.customers(id) ON DELETE CASCADE,
    subscription_id UUID REFERENCES payment.subscriptions(id) ON DELETE SET NULL,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(50) NOT NULL,
    description TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for payment.transactions
CREATE INDEX IF NOT EXISTS idx_transactions_customer_id ON payment.transactions(customer_id);
CREATE INDEX IF NOT EXISTS idx_transactions_subscription_id ON payment.transactions(subscription_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON payment.transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON payment.transactions(created_at DESC);

-- Create webhook events table for idempotency
CREATE TABLE IF NOT EXISTS payment.webhook_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    stripe_event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

-- Create index for webhook events
CREATE INDEX IF NOT EXISTS idx_webhook_events_stripe_id ON payment.webhook_events(stripe_event_id);
CREATE INDEX IF NOT EXISTS idx_webhook_events_processed ON payment.webhook_events(processed);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON auth.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON payment.customers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON payment.subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON payment.transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (for development)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO catalyst;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA payment TO catalyst;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA auth TO catalyst;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA audit TO catalyst;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO catalyst;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA payment TO catalyst;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA auth TO catalyst;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA audit TO catalyst;

-- Log initialization
INSERT INTO audit.event_log (event_type, entity_type, entity_id, event_data)
VALUES ('DATABASE_INITIALIZED', 'SYSTEM', 'catalyst_db', '{"version": "1.0.0", "timestamp": "' || CURRENT_TIMESTAMP || '"}');

-- Display completion message
DO $$
BEGIN
    RAISE NOTICE 'Catalyst database initialized successfully!';
    RAISE NOTICE 'Schemas created: public, payment, auth, audit';
    RAISE NOTICE 'Extensions enabled: uuid-ossp, pgcrypto';
END $$;


