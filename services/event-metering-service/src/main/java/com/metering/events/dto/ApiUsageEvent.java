package com.metering.events.dto;
import java.time.Instant;

public record ApiUsageEvent(
        String eventId,
        String customerId,
        String apiEndpoint,
        long responseTimeMs,
        long tokensUsed,
        int statusCode,
        Instant timeStamp
) {
}