package com.metering.invoice.controller;

import com.metering.invoice.client.AggregationEngineClient;
import com.metering.invoice.dto.CustomerUsageSummary;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceTestController {

    private final AggregationEngineClient aggregationClient;

    public InvoiceTestController(AggregationEngineClient aggregationClient) {
        this.aggregationClient = aggregationClient;
    }

    @PostMapping("/test-fetch-usage")
    public List<CustomerUsageSummary> testFetchUsage() {
        Instant end = Instant.now();
        Instant start = end.minus(30, ChronoUnit.DAYS);
        return aggregationClient.getUsageSummaries(start, end);
    }
}