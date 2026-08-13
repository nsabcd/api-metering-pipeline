package com.metering.aggregation.controller;

import com.metering.aggregation.dto.ApiUsageEvent;
import com.metering.aggregation.dto.CustomerUsageSummary;
import com.metering.aggregation.entity.UsageEventEntity;
import com.metering.aggregation.repository.UsageEventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usage")
public class AggregationController  {
    private final UsageEventRepository usageEventRepository;

    public AggregationController(UsageEventRepository usageEventRepository) {
        this.usageEventRepository = usageEventRepository;
    }

    @GetMapping("/summary")
    public List<CustomerUsageSummary> getUsageSummary(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
    ){
        return usageEventRepository.findUsageSummaryByPeriod(start, end);
    }
}
