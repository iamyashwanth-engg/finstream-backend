package com.banking.finstream.finstream_backend.producer;

import com.banking.finstream.finstream_backend.config.KafkaTopicConfig;
import com.banking.finstream.finstream_backend.event.PaymentSettledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentSettled(PaymentSettledEvent event) {
        event.setEventId(UUID.randomUUID().toString());
        log.info("[MOCK EVENT] Payment settled for txId: {}. Kafka publish bypassed.", event.getTransactionId());
//        log.info("Emitting PaymentSettledEvent to Kafka [topic={}, txId={}, debtor={}]",
//                KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, event.getTransactionId(), event.getSourceAccountId());
//
//        // Key = sourceAccountId (preserves per-account ordering)
//        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, event.getSourceAccountId(), event)
//                .whenComplete((result, ex) -> {
//                    if (ex == null) {
//                        log.info("Kafka ACK received: offset={}, partition={}",
//                                result.getRecordMetadata().offset(),
//                                result.getRecordMetadata().partition());
//                    } else {
//                        log.error("Failed to publish payment event to Kafka for txId: {}", event.getTransactionId(), ex);
//                    }
//                });
    }
}
