package com.metering.aggregation.repository;

import com.metering.aggregation.dto.CustomerUsageSummary;
import com.metering.aggregation.entity.UsageEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEventEntity, UsageEventEntity.UsageEventId> {

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