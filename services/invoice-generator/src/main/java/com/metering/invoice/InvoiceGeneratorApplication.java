package com.metering.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvoiceGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(InvoiceGeneratorApplication.class, args);
    }
}