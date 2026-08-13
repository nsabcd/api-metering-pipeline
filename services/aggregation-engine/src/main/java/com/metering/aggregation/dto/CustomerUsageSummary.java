package com.metering.aggregation.dto;

public record CustomerUsageSummary(
        String customerId,
        long totalTokens,
        long totalRequests
) {
}