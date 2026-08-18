package com.metering.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Configuration
public class RateLimiterConfig {

    @Value("${rate-limiter.enabled:true}")
    private boolean rateLimiterEnabled;

    @Bean
    public KeyResolver customerKeyResolver(){
        // Uses the mutated X-Customer-Id header or falls back to IP address
        return exchenge -> {
            // Generates a new bucket per request to bypass throttling entirely
            if (!rateLimiterEnabled) {
                return Mono.just(UUID.randomUUID().toString());
            }

            String customerId = exchenge.getRequest().getHeaders().getFirst("X-Customer-Id");
            if(customerId != null && !customerId.isBlank()){
                return Mono.just(customerId);
            }
            return Mono.just(
                    exchenge.getRequest().getRemoteAddress()!=null?
                            exchenge.getRequest().getRemoteAddress().getAddress().getHostAddress():"anonymous"
            );
        };
    }
}
