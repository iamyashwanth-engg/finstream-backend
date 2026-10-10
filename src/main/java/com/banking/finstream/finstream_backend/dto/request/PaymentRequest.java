package com.banking.finstream.finstream_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PaymentRequest {
    @NotBlank(message = "Debitor account is required")
    private String sourceAccountId;

    @NotBlank(message = "Creditor counterparty account is required")
    private String destinationAccountId;

    @NotNull(message = "payment cannot be null")
    @DecimalMin(value = "0.01", message="Amoount must be strictly positive")
    private BigDecimal amount;

    @NotBlank(message="Currency-code is required")
    @Pattern(regexp = "[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., GBP, USD)")
    private String currencyCode;

    public String getCurrency() {
        return currencyCode;
    }
}
