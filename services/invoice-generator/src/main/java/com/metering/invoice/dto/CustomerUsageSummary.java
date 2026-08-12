package com.metering.invoice.dto;

public record CustomerUsageSummary(
        String customerId,
        long totalTokens,
        long totalRequests
) {}