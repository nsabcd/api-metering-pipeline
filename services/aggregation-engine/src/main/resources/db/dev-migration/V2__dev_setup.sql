-- 1. Remove policy & drop existing view for clean local iteration
SELECT remove_continuous_aggregate_policy('hourly_customer_usage', if_exists => TRUE);
DROP MATERIALIZED VIEW IF EXISTS hourly_customer_usage CASCADE;

-- 2. Re-create Materialized View cleanly
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
GROUP BY bucket, customer_id, api_endpoint;

-- 3. Re-add background policy
PERFORM add_continuous_aggregate_policy('hourly_customer_usage',
    start_offset => INTERVAL '3 days',
    end_offset   => INTERVAL '1 hour',
    schedule_interval => INTERVAL '15 minutes');

-- 4. Seed dummy data for testing
INSERT INTO api_usage_events (event_id, customer_id, api_endpoint, response_time_ms, tokens_used, status_code, timestamp)
VALUES
  ('evt-101', 'cust_demo_123', '/api/v1/generate-text', 120, 1500, 200, NOW() - INTERVAL '2 hours'),
  ('evt-102', 'cust_demo_123', '/api/v1/generate-text', 95, 800, 200, NOW() - INTERVAL '1 hour')
ON CONFLICT DO NOTHING;

-- 5. Force IMMEDIATE refresh so test data shows up right away in DEV
CALL refresh_continuous_aggregate('hourly_customer_usage', NOW() - INTERVAL '3 hours', NOW());