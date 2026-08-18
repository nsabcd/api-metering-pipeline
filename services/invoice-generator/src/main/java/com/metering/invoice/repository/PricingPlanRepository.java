package com.metering.invoice.repository;

import com.metering.invoice.entity.PricingPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingPlanRepository extends JpaRepository<PricingPlanEntity, String> {}
