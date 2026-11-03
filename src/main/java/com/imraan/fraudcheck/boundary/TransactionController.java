package com.imraan.fraudcheck.boundary;

import com.imraan.fraudcheck.control.TransactionService;
import com.imraan.fraudcheck.entity.FraudResultResponse;
import com.imraan.fraudcheck.entity.TransactionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<FraudResultResponse> submit(@Valid @RequestBody TransactionRequest request) {
        FraudResultResponse response = transactionService.submit(request);
        URI location = URI.create("/api/transactions/" + response.transactionId() + "/fraud-result");
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}/fraud-result")
    public FraudResultResponse getFraudResult(@PathVariable UUID id) {
        return transactionService.getResult(id);
    }
}
