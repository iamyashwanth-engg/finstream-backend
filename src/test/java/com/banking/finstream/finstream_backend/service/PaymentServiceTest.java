package com.banking.finstream.finstream_backend.service;

import com.banking.finstream.finstream_backend.domain.entity.Account;
import com.banking.finstream.finstream_backend.domain.entity.transaction; // Change to Transaction if your class is uppercase
import com.banking.finstream.finstream_backend.dto.request.PaymentRequest;
import com.banking.finstream.finstream_backend.dto.response.PaymentResponse;
import com.banking.finstream.finstream_backend.exception.InsufficeintFundException;
import com.banking.finstream.finstream_backend.producer.PaymentEventProducer;
import com.banking.finstream.finstream_backend.repository.AccountRepository;
import com.banking.finstream.finstream_backend.repository.TranscationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TranscationRepository transactionRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .sourceAccountId("ACC-1001")
                .destinationAccountId("ACC-1002")
                .amount(new BigDecimal("100.00"))
                .currencyCode("GBP")
                .build();

        sourceAccount = Account.builder()
                .id("ACC-1001")
                .balance(new BigDecimal("500.00"))
                .currency("GBP")
                .build();

        destinationAccount = Account.builder()
                .id("ACC-1002")
                .balance(new BigDecimal("1000.00"))
                .currency("GBP")
                .build();
    }

    @Test
    void processPayment_SuccessfulSettlement() {
        // Arrange
        when(transactionRepository.findByIdempotencyKey("KEY-123")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate("ACC-1001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("ACC-1002")).thenReturn(Optional.of(destinationAccount));

        transaction savedTx = new transaction();
        savedTx.setId("TXN-999");
        savedTx.setIdempotencyKey("KEY-123");
        when(transactionRepository.save(any(transaction.class))).thenReturn(savedTx);

        // Act
        PaymentResponse response = paymentService.processPayment("KEY-123", paymentRequest);

        // Assert
        assertNotNull(response);
        assertEquals("TXN-999", response.getTranscationId());
        assertEquals(new BigDecimal("400.00"), sourceAccount.getBalance()); // 500 - 100
        assertEquals(new BigDecimal("1100.00"), destinationAccount.getBalance()); // 1000 + 100

        // Verify database saves and Kafka event were triggered
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any(transaction.class));
        // Uncomment this when Kafka is re-enabled:
        // verify(paymentEventProducer, times(1)).publishPaymentSettled(any());
    }

    @Test
    void processPayment_InsufficientFunds_ThrowsException() {
        // Arrange
        sourceAccount.setBalance(new BigDecimal("50.00")); // Less than the 100.00 requested
        when(transactionRepository.findByIdempotencyKey("KEY-124")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate("ACC-1001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("ACC-1002")).thenReturn(Optional.of(destinationAccount));

        // Act & Assert
        assertThrows(InsufficeintFundException.class, () ->
                paymentService.processPayment("KEY-124", paymentRequest)
        );

        // Verify ledger was NOT mutated
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(transaction.class));
    }

    @Test
    void processPayment_IdempotentRequest_ReturnsExistingTransaction() {
        // Arrange
        transaction existingTx = new transaction();
        existingTx.setId("TXN-OLD");
        when(transactionRepository.findByIdempotencyKey("KEY-125")).thenReturn(Optional.of(existingTx));

        // Act
        PaymentResponse response = paymentService.processPayment("KEY-125", paymentRequest);

        // Assert
        assertEquals("TXN-OLD", response.getTranscationId());
        assertEquals("Transaction previously processed (Idempotent response)", response.getMessage());

        // Verify no new processing happened
        verify(accountRepository, never()).findByIdForUpdate(anyString());
    }
    @Test
    void processPayment_ExactBalance_SuccessfuSettlement(){
        sourceAccount.setBalance(new BigDecimal("100.00"));
        when(transactionRepository.findByIdempotencyKey("KEY-124")).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate("ACC-1001")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("ACC-1002")).thenReturn(Optional.of(destinationAccount));
        
        transaction savedTx = new transaction();
        savedTx.setId("TXN-100");
        when(transactionRepository.save(any(transaction.class))).thenReturn(savedTx);
        
        //Act
        
        PaymentResponse response = paymentService.processPayment("KEY-BOUNDARY",paymentRequest);
        
        // Assert -if Pitest messes with the boundary condition, this will fail!
        assertNotNull(response);
        assertEquals(new BigDecimal("0.00"), sourceAccount.getBalance());
        verify(accountRepository, times(1)).save(any(Account.class));
    }
}