package com.metering.invoice.config;

import com.metering.invoice.job.MonthlyInvoiceJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    @Bean
    public JobDetail monthlyInvoiceJogDetail(){
        return JobBuilder.newJob(MonthlyInvoiceJob.class)
                .withIdentity("monthlyInvoiceJob", "billingGroup")
                .withDescription("Generates monthly usage invoices for all active customers")
                .storeDurably()
                .build();
    }
    @Bean
    public Trigger monthlyInvoiceJobTrigger(JobDetail monthlyInvoiceJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(monthlyInvoiceJobDetail)
                .withIdentity("monthlyInvoiceTrigger", "billingGroup")
                // Runs at midnight on the 1st day of every month: "0 0 0 1 * ?"
                // For dev/testing, runs every 5 minutes: "0 0/5 * * * ?"
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/5 * * * ?"))
                .build();
    }
}
