package com.metering.invoice.service;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal calculateCost(String customerId, long totalTokens);
}
