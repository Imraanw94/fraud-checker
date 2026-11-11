package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.*;
import com.imraan.fraudcheck.entity.dto.FraudResultResponse;
import com.imraan.fraudcheck.entity.dto.TransactionRequest;
import com.imraan.fraudcheck.entity.exceptions.ResourceNotFoundException;
import com.imraan.fraudcheck.entity.repository.FraudResultRepository;
import com.imraan.fraudcheck.entity.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final FraudResultRepository fraudResultRepository;
    private final FraudEvaluator fraudEvaluator;

    public TransactionService(TransactionRepository transactionRepository,
                              FraudResultRepository fraudResultRepository,
                              FraudEvaluator fraudEvaluator) {
        this.transactionRepository = transactionRepository;
        this.fraudResultRepository = fraudResultRepository;
        this.fraudEvaluator = fraudEvaluator;
    }

    @Transactional
    public FraudResultResponse submit(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(request.accountId());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency().toUpperCase(java.util.Locale.ROOT));
        transaction.setMerchant(request.merchant());
        transaction.setCountry(request.country());
        transactionRepository.save(transaction);
        log.info("Transaction persisted: transactionId={} accountId={} amount={} currency={}",
                transaction.getId(), transaction.getAccountId(), transaction.getAmount(), transaction.getCurrency());

        Evaluation evaluation = fraudEvaluator.evaluateTransaction(transaction);
        FraudResult result = new FraudResult();
        result.setTransaction(transaction);
        result.setRiskScore(evaluation.score());
        result.setDecision(fraudEvaluator.decide(evaluation.score()));
        result.setTriggeredRules(evaluation.triggeredRules());

        FraudResult saved = fraudResultRepository.save(result);
        log.info("Fraud evaluation completed: transactionId={} riskScore={} decision={} triggeredRules={}",
                transaction.getId(), result.getRiskScore(), result.getDecision(), result.getTriggeredRules());
        return FraudResultResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FraudResultResponse getResult(UUID transactionId) {
        log.debug("Looking up fraud result: transactionId={}", transactionId);
        return fraudResultRepository.findByTransactionId(transactionId)
                .map(FraudResultResponse::from)
                .orElseThrow(() -> {
                    log.warn("Fraud result not found: transactionId={}", transactionId);
                    return new ResourceNotFoundException("No fraud result for transaction " + transactionId);
                });
    }
}
