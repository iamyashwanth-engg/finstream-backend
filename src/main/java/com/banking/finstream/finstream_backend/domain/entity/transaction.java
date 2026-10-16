package com.banking.finstream.finstream_backend.domain.entity;

import com.banking.finstream.finstream_backend.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="TRANSACTIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class transaction {
    @Id
    @Column(name="id", length=36, nullable = false)
    private String id;

    @Column(name="idempotency_key", length = 128, nullable = false, unique=true)
    private String idempotencyKey;

    @Column(name="account_id", length = 64, nullable=false)
    private String accountId;

    @Column(name="counterparty_account", length = 64, nullable=false)
    private String counterpartyAccount;

    @Column(name="amount", precision=15, scale=2, nullable=false)
    private BigDecimal amount;

    @Column(name="currency", length=64, nullable=false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
