package com.metering.events.config;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.metering.events.dto.ApiUsageEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.function.Supplier;

import java.util.Map;


public class KafkaProducerConfig {


    public ProducerFactory<String, ApiUsageEvent> producerFactory(KafkaProperties kafkaProperties){
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties(null);

        configProps.put(ProducerConfig.ACKS_CONFIG, "1");
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);

        // Configure Jackson ObjectMapper for java.time.Instant support
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Explicitly type the Suppliers to prevent generic type inference issues
        Supplier<Serializer<String>> keySerializerSupplier = StringSerializer::new;
        Supplier<Serializer<ApiUsageEvent>> valueSerializerSupplier = () -> {
            JsonSerializer<ApiUsageEvent> serializer = new JsonSerializer<>(objectMapper);
            serializer.setAddTypeInfo(true);
            return serializer;
        };

        return new DefaultKafkaProducerFactory<>(
                configProps,
                keySerializerSupplier,
                valueSerializerSupplier
        );
    }


    public KafkaTemplate<String, ApiUsageEvent> kafkaTemplate(ProducerFactory<String, ApiUsageEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
