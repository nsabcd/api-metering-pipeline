package com.metering.aggregation.repository;

import com.metering.aggregation.dto.CustomerUsageSummary;
import com.metering.aggregation.entity.UsageEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEventEntity, UsageEventEntity.UsageEventId> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO api_usage_events (event_id, customer_id, api_endpoint, response_time_ms, tokens_used, status_code, timestamp)
        VALUES (:eventId, :customerId, :apiEndpoint, :responseTimeMs, :tokensUsed, :statusCode, :timestamp)
        ON CONFLICT (event_id, timestamp) DO NOTHING
    """, nativeQuery = true)
    int saveIdempotent(
            @Param("eventId") String eventId,
            @Param("customerId") String customerId,
            @Param("apiEndpoint") String apiEndpoint,
            @Param("responseTimeMs") long responseTimeMs,
            @Param("tokensUsed") long tokensUsed,
            @Param("statusCode") int statusCode,
            @Param("timestamp") Instant timestamp
    );

    @Query("""
        SELECT new com.metering.aggregation.dto.CustomerUsageSummary(
            u.customerId,
            COALESCE(SUM(u.totalTokens), 0),
            COALESCE(SUM(u.totalRequests), 0)
        )
        FROM CustomerHourlyUsageEntity u
        WHERE u.bucket BETWEEN :start AND :end
        GROUP BY u.customerId
    """)
    List<CustomerUsageSummary> findUsageSummaryByPeriod(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}