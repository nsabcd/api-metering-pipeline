package com.metering.invoice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_plan_mappings")
public class CustomerPlanMappingEntity {

    @Id
    private String customerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private PricingPlanEntity pricingPlan;

    public CustomerPlanMappingEntity() {}

    public CustomerPlanMappingEntity(String customerId, PricingPlanEntity pricingPlan) {
        this.customerId = customerId;
        this.pricingPlan = pricingPlan;
    }

    public String getCustomerId() { return customerId; }
    public PricingPlanEntity getPricingPlan() { return pricingPlan; }
    public void setPricingPlan(PricingPlanEntity pricingPlan) { this.pricingPlan = pricingPlan; }
}