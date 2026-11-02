package com.imraan.fraudcheck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "fraud_results")
@Getter
@Setter
@NoArgsConstructor
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision;

    @Column(nullable = false)
    private int riskScore;

    @ManyToMany
    @JoinTable(name = "fraud_result_triggered_rules",
            joinColumns = @JoinColumn(name = "fraud_result_id"),
            inverseJoinColumns = @JoinColumn(name = "fraud_rule_id"))
    private Set<FraudRule> triggeredRules = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant evaluatedAt;

    @PrePersist
    void onCreate() {
        if (evaluatedAt == null) {
            evaluatedAt = Instant.now();
        }
    }
}
