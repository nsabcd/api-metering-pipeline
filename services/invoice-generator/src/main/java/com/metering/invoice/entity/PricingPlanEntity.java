package com.metering.invoice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pricing_plans")
public class PricingPlanEntity {

    @Id
    private String planId; // e.g., "DEFAULT", "ENTERPRISE", "PAY_AS_YOU_GO"

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal pricePerToken;

    @Column(nullable = false, precision = 12, scale = 8)
    private BigDecimal tieredDiscountRate; // Discount applied after tier threshold

    private Long tierThresholdTokens; // e.g., 1,000,000 tokens

    public PricingPlanEntity() {}

    public PricingPlanEntity(String planId, BigDecimal pricePerToken, BigDecimal tieredDiscountRate, Long tierThresholdTokens) {
        this.planId = planId;
        this.pricePerToken = pricePerToken;
        this.tieredDiscountRate = tieredDiscountRate;
        this.tierThresholdTokens = tierThresholdTokens;
    }

    public String getPlanId() { return planId; }
    public BigDecimal getPricePerToken() { return pricePerToken; }
    public BigDecimal getTieredDiscountRate() { return tieredDiscountRate; }
    public Long getTierThresholdTokens() { return tierThresholdTokens; }
}