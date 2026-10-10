package com.banking.finstream.finstream_backend.service;

import com.banking.finstream.finstream_backend.domain.entity.Account;
import com.banking.finstream.finstream_backend.domain.entity.transaction;
import com.banking.finstream.finstream_backend.domain.enums.TransactionStatus;
import com.banking.finstream.finstream_backend.dto.request.PaymentRequest;
import com.banking.finstream.finstream_backend.dto.response.PaymentResponse;
import com.banking.finstream.finstream_backend.exception.AccountNotFoundException;
import com.banking.finstream.finstream_backend.exception.InsufficeintFundException;
import com.banking.finstream.finstream_backend.repository.AccountRepository;
import com.banking.finstream.finstream_backend.repository.TranscationRepository;
import com.banking.finstream.finstream_backend.producer.PaymentEventProducer;
import com.banking.finstream.finstream_backend.event.PaymentSettledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final AccountRepository accountRepository;
    private final TranscationRepository transactionRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse processPayment(String idempotencyKey, PaymentRequest request) {
        log.info("Processing settlement for idempotencyKey: {}, account: {}", idempotencyKey, request.getSourceAccountId());

        // 1. Idempotency Check: Return existing record if already processed
        Optional<transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            log.warn("Duplicate request detected for key: {}. Returning recorded transaction.", idempotencyKey);
            return mapToResponse(existingTx.get(), "Transaction previously processed (Idempotent response)");
        }

        // 2. Fetch Source and Destination Accounts using Pessimistic Write Lock
        Account sourceAccount = accountRepository.findByIdForUpdate(request.getSourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Debtor account not found: " + request.getSourceAccountId()));

        Account destinationAccount = accountRepository.findByIdForUpdate(request.getDestinationAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Creditor account not found: " + request.getDestinationAccountId()));

        // 3. Balance & Currency Validation
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficeintFundException(String.format("Available balance (%s %s) is insufficient for transaction amount (%s %s)",
                    sourceAccount.getBalance(), sourceAccount.getCurrency(), request.getAmount(), request.getCurrency()));
        }

        // 4. Ledger Balance Mutation
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        // 5. Record Transaction Entry
        transaction tx = transaction.builder()
                .id(UUID.randomUUID().toString())
                .idempotencyKey(idempotencyKey)
                .accountId(request.getSourceAccountId())
                .counterpartyAccount(request.getDestinationAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(TransactionStatus.SETTLED)
                .version(0L)
                .build();
        transaction savedTx = transactionRepository.save(tx);
        log.info("Payment settled successfully. Transaction ID: {}", savedTx.getId());

        PaymentSettledEvent event = PaymentSettledEvent.builder()
                .transactionId(savedTx.getId())
                .idempotencyKey(savedTx.getIdempotencyKey())
                .sourceAccountId(savedTx.getAccountId())
                .destinationAccountId(savedTx.getCounterpartyAccount())
                .amount(savedTx.getAmount())
                .currency(savedTx.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();
        paymentEventProducer.publishPaymentSettled(event);

        return mapToResponse(savedTx, "Payment settled successfully");
    }

    private PaymentResponse mapToResponse(transaction tx, String message) {
        return PaymentResponse.builder()
                .transcationId(tx.getId())
                .idempotencyKey(tx.getIdempotencyKey())
                .sourceAccountId(tx.getAccountId())
                .destinationAccountId(tx.getCounterpartyAccount())
                .amount(tx.getAmount())
                .currency(tx.getCurrency())
                .status(tx.getStatus())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

