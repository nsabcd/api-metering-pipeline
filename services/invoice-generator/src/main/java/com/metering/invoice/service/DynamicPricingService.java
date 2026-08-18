package com.metering.invoice.service;

import com.metering.invoice.entity.CustomerPlanMappingEntity;
import com.metering.invoice.entity.PricingPlanEntity;
import com.metering.invoice.repository.CustomerPlanMappingRepository;
import com.metering.invoice.repository.PricingPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DynamicPricingService implements PricingService {

    private final PricingPlanRepository planRepository;
    private final CustomerPlanMappingRepository mappingRepository;

    private static final BigDecimal DEFAULT_PRICE = new BigDecimal("0.000002");

    public DynamicPricingService(PricingPlanRepository planRepository, CustomerPlanMappingRepository mappingRepository) {
        this.planRepository = planRepository;
        this.mappingRepository = mappingRepository;
    }

    @Override
    public BigDecimal calculateCost(String customerId, long totalTokens) {
        PricingPlanEntity plan = mappingRepository.findById(customerId)
                .map(CustomerPlanMappingEntity::getPricingPlan)
                .orElseGet(() -> planRepository.findById("DEFAULT").orElse(null));

        if (plan == null) {
            return BigDecimal.valueOf(totalTokens).multiply(DEFAULT_PRICE);
        }

        // Standard single rate
        if (plan.getTierThresholdTokens() == null || plan.getTieredDiscountRate() == null) {
            return BigDecimal.valueOf(totalTokens).multiply(plan.getPricePerToken());
        }

        // Dynamic tiered calculation
        long threshold = plan.getTierThresholdTokens();
        if (totalTokens <= threshold) {
            return BigDecimal.valueOf(totalTokens).multiply(plan.getPricePerToken());
        } else {
            BigDecimal baseCost = BigDecimal.valueOf(threshold).multiply(plan.getPricePerToken());
            BigDecimal discountedCost = BigDecimal.valueOf(totalTokens - threshold).multiply(plan.getTieredDiscountRate());
            return baseCost.add(discountedCost);
        }
    }

    @Transactional
    public PricingPlanEntity savePlan(PricingPlanEntity plan) {
        return planRepository.save(plan);
    }

    @Transactional
    public CustomerPlanMappingEntity assignPlanToCustomer(String customerId, String planId) {
        PricingPlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        return mappingRepository.save(new CustomerPlanMappingEntity(customerId, plan));
    }

    public List<PricingPlanEntity> getAllPlans() {
        return planRepository.findAll();
    }
}