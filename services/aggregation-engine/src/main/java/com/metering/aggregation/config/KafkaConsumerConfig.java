package com.metering.aggregation.config;

import com.metering.aggregation.dto.ApiUsageEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, ApiUsageEvent> consumerFactory (KafkaProperties kafkaProperties){
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Wrap JsonDeserializer with ErrorHandlingDeserializer
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());

        // Deserializer configuration
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.metering.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.metering.aggregation.dto.ApiUsageEvent");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Ignore type headers from producer to allow package name decoupling

        return new DefaultKafkaConsumerFactory<>(props);

    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ApiUsageEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, ApiUsageEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, ApiUsageEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Concurrency setting based on partitions
        factory.setConcurrency(3);
        return factory;
    }
}
