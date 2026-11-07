package com.imraan.fraudcheck.entity.repository;

import com.imraan.fraudcheck.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
}
