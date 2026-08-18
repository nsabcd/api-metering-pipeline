package com.metering.invoice.job;
import com.metering.invoice.client.AggregationEngineClient;
import com.metering.invoice.dto.CustomerUsageSummary;
import com.metering.invoice.entity.InvoiceEntity;
import com.metering.invoice.repository.InvoiceRepository;
import com.metering.invoice.service.PricingService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.quartz.QuartzJobBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class MonthlyInvoiceJob extends QuartzJobBean {
    private static final Logger log = LoggerFactory.getLogger(MonthlyInvoiceJob.class);

    // TODO: make pricing dynamic
    // Rate: $0.000002 per token ($2 per 1M tokens)
    private static final BigDecimal PRICE_PER_TOKEN = new BigDecimal("0.000002");

    private final InvoiceRepository invoiceRepository;
    private final AggregationEngineClient aggregationClient;
    private final PricingService pricingService;

    public MonthlyInvoiceJob(InvoiceRepository invoiceRepository,
                             AggregationEngineClient aggregationClient,
                             PricingService pricingService){
        this.invoiceRepository=invoiceRepository;
        this.aggregationClient = aggregationClient;
        this.pricingService=pricingService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting monthly invoice generation job...");
        Instant periodEnd = Instant.now();
        Instant periodStart = periodEnd .minus(30, ChronoUnit.DAYS);
        log.info("Starting monthly invoice generation for period: {} to {}", periodStart, periodEnd);
        try{
            // 1. Fetch real usage data aggregated per customer from the aggregation-engine
            List<CustomerUsageSummary> usageSummaries =
                    aggregationClient.getUsageSummaries(periodStart, periodEnd);
            if (usageSummaries.isEmpty()) {
                log.info("No active usage recorded during this billing cycle.");
                return;
            }
            int generatedCount = 0;
            // 2. Iterate through each real customer record
            for (CustomerUsageSummary usage : usageSummaries) {
                if (usage.totalTokens() <= 0) {
                    log.debug("Skipping zero-usage customer [{}]", usage.customerId());
                    continue;
                }

                // 3. Compute total billing amount using exact BigDecimal scale
                BigDecimal totalAmount = pricingService
                        .calculateCost(usage.customerId(), usage.totalTokens())
                        .setScale(2, RoundingMode.HALF_UP);

                // 4. Construct unique, persistent invoice entity
                InvoiceEntity invoice = new InvoiceEntity(
                        "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        usage.customerId(),
                        totalAmount,
                        usage.totalTokens(),
                        "PENDING",
                        periodStart,
                        periodEnd,
                        periodEnd
                );

                invoiceRepository.save(invoice);
                generatedCount++;

                log.info("Successfully generated Invoice [{}] for Customer [{}] | Tokens: {} | Amount: ${}",
                        invoice.getInvoiceId(), usage.customerId(), usage.totalTokens(), totalAmount);
            }
            log.info("Monthly invoice job completed. Generated {} invoices.", generatedCount);
        }catch (Exception e) {
            log.error("Failed to execute monthly invoice generation job", e);
            throw new JobExecutionException("Invoice job failed", e);
        }
    }
}
