package com.metering.aggregation.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "api_usage_events")
@IdClass(UsageEventEntity.UsageEventId.class)
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

    @Id
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

    // Composite Key Class matching (event_id, timestamp)
    public static class UsageEventId implements Serializable{
        private String eventId;
        private Instant timestamp;

        public UsageEventId() {
        }

        public UsageEventId(String eventId, Instant timestamp) {
            this.eventId = eventId;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o){
            if (this==o) return true;
            if(o==null || getClass() != o.getClass()) return false;
            UsageEventId that = (UsageEventId) o;
            return Objects.equals(eventId, that.eventId) && Objects.equals(timestamp, that.timestamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, timestamp);
        }
    }
}
