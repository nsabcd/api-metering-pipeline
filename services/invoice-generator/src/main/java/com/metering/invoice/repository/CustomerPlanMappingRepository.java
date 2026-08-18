package com.metering.invoice.repository;

import com.metering.invoice.entity.CustomerPlanMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPlanMappingRepository extends JpaRepository<CustomerPlanMappingEntity, String> {}