package com.metering.invoice.controller;

import com.metering.invoice.entity.CustomerPlanMappingEntity;
import com.metering.invoice.entity.PricingPlanEntity;
import com.metering.invoice.service.DynamicPricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pricing")
@PreAuthorize("hasRole('ADMIN')")
public class PricingAdminController {

    private final DynamicPricingService pricingService;

    public PricingAdminController(DynamicPricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/plans")
    public ResponseEntity<PricingPlanEntity> createOrUpdatePlan(@RequestBody PricingPlanEntity plan) {
        return ResponseEntity.ok(pricingService.savePlan(plan));
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PricingPlanEntity>> getAllPlans() {
        return ResponseEntity.ok(pricingService.getAllPlans());
    }

    @PostMapping("/customers/{customerId}/assign")
    public ResponseEntity<CustomerPlanMappingEntity> assignPlan(
            @PathVariable String customerId,
            @RequestParam String planId) {
        return ResponseEntity.ok(pricingService.assignPlanToCustomer(customerId, planId));
    }
}