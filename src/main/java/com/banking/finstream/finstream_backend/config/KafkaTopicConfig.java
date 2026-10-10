package com.banking.finstream.finstream_backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

//@Configuration
public class KafkaTopicConfig {
    public static final String PAYMENT_EVENTS_TOPIC = "payment.events";

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
                .partitions(3)       // 3 partitions for horizontal consumer scale
                .replicas(1)         // 1 replica for local development (3 in AWS MSK)
                .build();
    }
}
