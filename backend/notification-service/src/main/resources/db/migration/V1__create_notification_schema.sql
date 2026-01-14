-- Create notification schema
CREATE SCHEMA IF NOT EXISTS notification;

-- Notification log table
CREATE TABLE notification.notification_log (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_notification_log_recipient ON notification.notification_log(recipient_email);
CREATE INDEX idx_notification_log_status ON notification.notification_log(status);
CREATE INDEX idx_notification_log_created_at ON notification.notification_log(created_at);
CREATE INDEX idx_notification_log_type ON notification.notification_log(type);

