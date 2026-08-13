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
public interface UsageEventRepository  extends JpaRepository<UsageEventEntity, String> {
    @Query("""
        SELECT new com.metering.aggregation.dto.CustomerUsageSummary(
            e.customerId,
            COALESCE(SUM(e.tokensUsed), 0),
            COUNT(e)
        )
        FROM UsageEventEntity e
        WHERE e.timestamp BETWEEN :start AND :end
        GROUP BY e.customerId
    """)
    List<CustomerUsageSummary> findUsageSummaryByPeriod(
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
