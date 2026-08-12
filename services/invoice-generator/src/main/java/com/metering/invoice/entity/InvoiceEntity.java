package com.metering.invoice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoices")
public class InvoiceEntity {
    @Id
    private String invoiceId;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    private long totalTokens;

    @Column(nullable = false)
    private String status; // PENDING, PAID, FAILED

    @Column(nullable = false)
    private Instant periodStart;

    @Column(nullable = false)
    private Instant periodEnd;

    private Instant createdAt;

    public InvoiceEntity() {
    }

    public InvoiceEntity(
            String invoiceId,
            String customerId,
            BigDecimal totalAmount,
            long totalTokens,
            String status,
            Instant periodStart,
            Instant periodEnd,
            Instant createdAt) {
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.totalTokens = totalTokens;
        this.status = status;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdAt = createdAt;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(long totalTokens) {
        this.totalTokens = totalTokens;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
