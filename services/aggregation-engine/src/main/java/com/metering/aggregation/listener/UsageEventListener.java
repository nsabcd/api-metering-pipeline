package com.metering.aggregation.listener;

import com.metering.aggregation.dto.ApiUsageEvent;
import com.metering.aggregation.entity.UsageEventEntity;
import com.metering.aggregation.repository.UsageEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UsageEventListener {
    private static final Logger log = LoggerFactory.getLogger(UsageEventListener.class);
    private final UsageEventRepository repository;

    public UsageEventListener(UsageEventRepository repository){
        this.repository=repository;
    }

    @KafkaListener(
            topics = "api-usage-events",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeevent(ApiUsageEvent event){
        log.info("Received event for Customer: [{}] | Endpoint: [{}] | Tokens: [{}]",
                event.customerId(), event.apiEndpoint(), event.tokensUsed());

        UsageEventEntity usageEventEntity = new UsageEventEntity(
                event.eventId(),
                event.customerId(),
                event.apiEndpoint(),
                event.responseTimeMs(),
                event.tokensUsed(),
                event.statusCode(),
                event.timestamp()
        );

        repository.save(usageEventEntity);
    }
}
