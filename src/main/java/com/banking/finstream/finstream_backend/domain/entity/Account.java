package com.banking.finstream.finstream_backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime; // <--- THIS IS THE CRITICAL MISSING IMPORT

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "owner_name", length = 128, nullable = false)
    private String ownerName;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Explicitly define the column type to prevent Hibernate parsing issues
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}