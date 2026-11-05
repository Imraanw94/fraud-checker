package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.FraudResult;
import com.imraan.fraudcheck.entity.FraudResultRepository;
import com.imraan.fraudcheck.entity.FraudResultResponse;
import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.TransactionRepository;
import com.imraan.fraudcheck.entity.TransactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TransactionService {

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

        FraudEvaluator.Evaluation evaluation = fraudEvaluator.evaluate(transaction);
        FraudResult result = new FraudResult();
        result.setTransaction(transaction);
        result.setRiskScore(evaluation.score());
        result.setDecision(fraudEvaluator.decide(evaluation.score()));
        result.setTriggeredRules(evaluation.triggeredRules());

        return FraudResultResponse.from(fraudResultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public FraudResultResponse getResult(UUID transactionId) {
        return fraudResultRepository.findByTransactionId(transactionId)
                .map(FraudResultResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No fraud result for transaction " + transactionId));
    }
}
