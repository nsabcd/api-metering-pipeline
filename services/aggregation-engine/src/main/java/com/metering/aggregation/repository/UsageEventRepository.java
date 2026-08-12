package com.metering.aggregation.repository;

import com.metering.aggregation.entity.UsageEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageEventRepository  extends JpaRepository<UsageEventEntity, String> {
}
