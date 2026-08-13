CREATE TABLE IF NOT EXISTS api_usage_events (
    event_id VARCHAR(255) NOT NULL,
        customer_id VARCHAR(255) NOT NULL,
        api_endpoint VARCHAR(255) NOT NULL,
        response_time_ms BIGINT NOT NULL,
        tokens_used BIGINT NOT NULL,
        status_code INT NOT NULL,
        timestamp TIMESTAMPTZ NOT NULL,
        PRIMARY KEY (event_id, timestamp)
);

-- Convert standard PostgreSQL table into a TimescaleDB hypertable partitioned by time
SELECT create_hypertable('api_usage_events', 'timestamp', if_not_exists => TRUE);

-- Safely create Continuous Aggregate View if it does not exist
CREATE MATERIALIZED VIEW IF NOT EXISTS hourly_customer_usage
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket,
    customer_id,
    api_endpoint,
    COUNT(*) AS total_requests,
    SUM(tokens_used) AS total_tokens,
    AVG(response_time_ms) AS ave_response_time
FROM api_usage_events
GROUP BY bucket, customer_id, api_endpoint
WITH NO DATA;

-- Add continuous aggregate refresh policy (runs automatically in background)
SELECT add_continuous_aggregate_policy('hourly_customer_usage',
    start_offset => INTERVAL '1 month',
    end_offset   => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour',
    if_not_exists => TRUE);
