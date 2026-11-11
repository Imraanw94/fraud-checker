package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.entity.dto.FraudResultResponse;
import com.imraan.fraudcheck.entity.dto.TransactionRequest;
import com.imraan.fraudcheck.entity.enums.FraudDecision;
import com.imraan.fraudcheck.entity.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "test-data.enabled", havingValue = "true")
public class StartupTestDataGenerator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupTestDataGenerator.class);

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public StartupTestDataGenerator(TransactionRepository transactionRepository, TransactionService transactionService) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (transactionRepository.count() >= 5) {
            log.info("Skipping test data generation because 5 or more transactions already exist");
            return;
        }

        generate(1, 10, "ZA", "Acme Store");
        generate(11, 20, "US", "Acme Store");
        generate(21, 30, "ZA", "ShadyMerchant");
        log.info("Generated test transactions: 10 approved, 10 in review, 10 rejected");
    }

    private void generate(int first, int last, String country, String merchant) {
        for (int i = first; i <= last; i++) {
            TransactionRequest request = new TransactionRequest(
                    "TEST-ACC-" + i, new BigDecimal("250.00"), "ZAR", merchant, country);
            transactionService.submit(request);
        }
    }
}
