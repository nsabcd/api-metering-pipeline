package com.metering.aggregation.listener;

import com.metering.aggregation.dto.ApiUsageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UsageEventListener {
    private static final Logger log = LoggerFactory.getLogger(UsageEventListener.class);

    @KafkaListener(
            topics = "api-usage-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeevent(ApiUsageEvent event){
        log.info("Received event for Customer: [{}] | Endpoint: [{}] | Tokens: [{}]",
                event.customerId(), event.apiEndpoint(), event.tokensUsed());

        // TODO: Next step - persist event to TimescaleDB hypertable
    }
}
