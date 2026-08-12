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
