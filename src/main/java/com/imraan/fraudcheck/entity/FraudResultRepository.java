package com.imraan.fraudcheck.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FraudResultRepository extends JpaRepository<FraudResult, UUID> {

    @Query("select r from FraudResult r join fetch r.transaction where r.transaction.id = :transactionId")
    Optional<FraudResult> findByTransactionId(UUID transactionId);
}
