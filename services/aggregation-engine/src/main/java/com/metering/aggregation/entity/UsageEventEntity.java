package com.metering.aggregation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "api_usage_events")
public class UsageEventEntity {
    @Id
    private String eventId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String apiEndpoint;

    private long responseTimeMs;
    private long tokensUsed;
    private int statusCode;

    @Column(nullable = false)
    private Instant timestamp;

    public UsageEventEntity() {
    }

    public UsageEventEntity(String eventId, String customerId,
                            String apiEndpoint, long responseTimeMs,
                            long tokensUsed, int statusCode, Instant timestamp) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.apiEndpoint = apiEndpoint;
        this.responseTimeMs = responseTimeMs;
        this.tokensUsed = tokensUsed;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public long getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(long tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
