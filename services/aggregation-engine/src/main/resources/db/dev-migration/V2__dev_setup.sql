-- flyway:executeInTransaction=false

-- 1. Drop existing view if resetting dev state
DROP MATERIALIZED VIEW IF EXISTS hourly_customer_usage CASCADE;

-- 2. Re-create Materialized View cleanly WITH NO DATA
CREATE MATERIALIZED VIEW hourly_customer_usage
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', timestamp) AS bucket,
    customer_id,
    api_endpoint,
    COUNT(*) AS total_requests,
    SUM(tokens_used) AS total_tokens,
    AVG(response_time_ms) AS avg_response_time
FROM api_usage_events
GROUP BY bucket, customer_id, api_endpoint
WITH NO DATA;

-- 3. Re-add refresh policy
SELECT add_continuous_aggregate_policy('hourly_customer_usage',
    start_offset => INTERVAL '1 month',
    end_offset   => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour',
    if_not_exists => TRUE);