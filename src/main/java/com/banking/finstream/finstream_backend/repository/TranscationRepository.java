package com.banking.finstream.finstream_backend.repository;

import com.banking.finstream.finstream_backend.domain.entity.transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TranscationRepository extends JpaRepository<transaction, String> {
    Optional<transaction> findByIdempotencyKey(String idempotencyKey);
}