package com.metering.invoice.client;

import com.metering.invoice.dto.CustomerUsageSummary;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.time.Instant;
import java.util.List;

@HttpExchange("/api/v1/usage")
public interface AggregationEngineClient {

    @GetExchange("/summary")
    List<CustomerUsageSummary> getUsageSummaries(
            @RequestParam("start") Instant start,
            @RequestParam("end") Instant end
            );
}
