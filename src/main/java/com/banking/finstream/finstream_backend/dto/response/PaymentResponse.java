package com.banking.finstream.finstream_backend.dto.response;

import com.banking.finstream.finstream_backend.domain.enums.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String transcationId;
    private String idempotencyKey;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String message;
    private LocalDateTime timestamp;
}
