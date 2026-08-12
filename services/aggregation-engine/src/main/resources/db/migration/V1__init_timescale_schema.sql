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
DO $$
BEGIN
    IF to_regclass('hourly_customer_usage') IS NULL THEN
        CREATE MATERIALIZED VIEW hourly_customer_usage
        WITH (timescaledb.continuous) AS
        SELECT
            time_bucket('1 hour', timestamp) AS bucket,
            customer_id,
            api_endpoint,
            COUNT(*) AS total_requests,
            SUM(tokens_used) AS total_tokens,
            AVG(response_time_ms) AS ave_response_time
        FROM api_usage_events
        GROUP BY bucket, customer_id, api_endpoint;
    END IF;
ENF $$;

-- Safely add continuous aggregate policy
DO $$
BEGIN
    IF NOT EXISTS(
        SELECT 1 FROM timescaledb_information.jobs
        WHERE proc_name = 'policy_refresh_continuous_aggregate'
              AND hypertable_name = 'hourly_customer_usage'
    ) THEN
        PERFORM add_continuous_aggregate_policy('hourly_customer_usage',
            start_offset => INTERVAL '3 days',
            end_offset   => INTERVAL '1 hour',
            schedule_interval => INTERVAL '15 minutes');
    END IF;
END $$;

