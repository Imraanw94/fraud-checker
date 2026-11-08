package com.imraan.fraudcheck.control;

import com.imraan.fraudcheck.TestcontainersConfiguration;
import com.imraan.fraudcheck.entity.ResourceNotFoundException;
import com.imraan.fraudcheck.entity.dto.FraudResultResponse;
import com.imraan.fraudcheck.entity.dto.TransactionRequest;
import com.imraan.fraudcheck.entity.repository.FraudResultRepository;
import com.imraan.fraudcheck.entity.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FraudResultRepository fraudResultRepository;

    @Test
    void submitPersistsTransactionAndResultInPostgres() {
        long transactionsBefore = transactionRepository.count();
        long resultsBefore = fraudResultRepository.count();

        FraudResultResponse response = transactionService.submit(
                new TransactionRequest("ACC-IT-1", new BigDecimal("250.00"), "zar", "Acme Store", "ZA"));

        assertThat(response.transactionId()).isNotNull();
        assertThat(transactionRepository.count()).isEqualTo(transactionsBefore + 1);
        assertThat(fraudResultRepository.count()).isEqualTo(resultsBefore + 1);
        assertThat(transactionService.getResult(response.transactionId()).decision())
                .isEqualTo(response.decision());
    }

    @Test
    void getResultThrowsWhenTransactionUnknown() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> transactionService.getResult(unknown))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
