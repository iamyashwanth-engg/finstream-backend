package com.banking.finstream.finstream_backend.consumer;

import com.banking.finstream.finstream_backend.config.KafkaTopicConfig;
import com.banking.finstream.finstream_backend.event.PaymentSettledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

//@Service
@Slf4j
public class FraudDetectionConsumer {
    private static final BigDecimal HIGH_RISK_THRESHOLD = new BigDecimal("10000.00");

    @KafkaListener(
            topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC,
            groupId = "finstream-settlement-group"
    )
    public void evaluateTransaction(@Payload PaymentSettledEvent event) {
        log.info("Fraud Engine evaluating transaction: ID={}, Amount={} {}",
                event.getTransactionId(), event.getAmount(), event.getCurrency());

        // Banking Fraud Rule 1: High-Value Settlement Alert
        if (event.getAmount().compareTo(HIGH_RISK_THRESHOLD) >= 0) {
            log.warn("🚨 [FRAUD ALERT] High-value wire flagged for manual review! TxId: {}, Debtor: {}, Amount: {} {}",
                    event.getTransactionId(), event.getSourceAccountId(), event.getAmount(), event.getCurrency());
        } else {
            log.info("✅ [FRAUD PASS] Transaction cleared standard risk heuristics. TxId: {}", event.getTransactionId());
        }
    }
}
