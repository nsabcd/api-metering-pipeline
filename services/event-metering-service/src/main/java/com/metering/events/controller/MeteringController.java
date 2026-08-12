package com.metering.events.controller;

import com.metering.events.dto.ApiUsageEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/events")
public class MeteringController {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "api-usage-events";

    public MeteringController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public ResponseEntity<Void> ingestEvent(@RequestBody ApiUsageEvent event){
        ApiUsageEvent enrichedEvent = new ApiUsageEvent(
                event.eventId()!=null?event.eventId(): UUID.randomUUID().toString(),
                event.customerId(),
                event.apiEndpoint(),
                event.responseTimeMs(),
                event.tokensUsed(),
                event.statusCode(),
                event.timeStamp()!=null?event.timeStamp(): Instant.now()
        );

        kafkaTemplate.send(TOPIC, enrichedEvent.customerId(), enrichedEvent);

        return ResponseEntity.accepted().build();
    }
}
