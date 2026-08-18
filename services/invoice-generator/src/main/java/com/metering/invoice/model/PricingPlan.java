package com.metering.invoice.model;

import java.math.BigDecimal;
import java.util.List;

public record PricingPlan(String planId, BigDecimal baseRate, List<PricingTier> tiers) {
    public BigDecimal calculateCost(long totalTokens) {
        if(tiers==null || tiers.isEmpty()){
            return BigDecimal.valueOf(totalTokens).multiply(baseRate);
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        long remainingTokens = totalTokens;
        for(PricingTier tier : tiers){
            if(remainingTokens < 0)
                break;
            long tierCapacity = (tier.maxTokens() == null)?
                    remainingTokens:Math.min(remainingTokens, tier.maxTokens()-tier.minTokens());
            if(tierCapacity>0){
                totalCost = totalCost.add(BigDecimal.valueOf(tierCapacity).multiply(tier.pricePerToken()));
                remainingTokens -= tierCapacity;
            }
        }
        return totalCost;
    }
}
