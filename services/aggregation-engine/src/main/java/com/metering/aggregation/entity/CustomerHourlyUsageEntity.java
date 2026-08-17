package com.metering.aggregation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "hourly_customer_usage")
@IdClass(CustomerHourlyUsageEntity.HourlyUsageId.class)
public class CustomerHourlyUsageEntity {

    @Id
    private Instant bucket;

    @Id
    private String customerId;

    @Id
    private String apiEndpoint;

    private long totalRequests;
    private long totalTokens;
    private double avgResponseTime;

    public CustomerHourlyUsageEntity() {}
    public Instant getBucket() { return bucket; }
    public String getCustomerId() { return customerId; }
    public String getApiEndpoint() { return apiEndpoint; }
    public long getTotalRequests() { return totalRequests; }
    public long getTotalTokens() { return totalTokens; }
    public double getAvgResponseTime() { return avgResponseTime; }

    // Composite Key Class
    public static class HourlyUsageId implements Serializable {
        private Instant bucket;
        private String customerId;
        private String apiEndpoint;

        public HourlyUsageId() {}

        public HourlyUsageId(Instant bucket, String customerId, String apiEndpoint) {
            this.bucket = bucket;
            this.customerId = customerId;
            this.apiEndpoint = apiEndpoint;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HourlyUsageId that = (HourlyUsageId) o;
            return Objects.equals(bucket, that.bucket) &&
                    Objects.equals(customerId, that.customerId) &&
                    Objects.equals(apiEndpoint, that.apiEndpoint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bucket, customerId, apiEndpoint);
        }
    }

}
