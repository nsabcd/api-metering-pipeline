package com.metering.invoice.model;

import java.math.BigDecimal;

public record PricingTier(Long minTokens, Long maxTokens, BigDecimal pricePerToken){}
