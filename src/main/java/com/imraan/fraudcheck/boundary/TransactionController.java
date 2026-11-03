package com.imraan.fraudcheck.boundary;

import com.imraan.fraudcheck.control.TransactionService;
import com.imraan.fraudcheck.entity.FraudResultResponse;
import com.imraan.fraudcheck.entity.TransactionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Transactions", description = "Submit transactions and retrieve fraud results")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Submit a transaction",
            description = "Persists the transaction, evaluates it against the fraud rules and returns the fraud result.")
    @ApiResponse(responseCode = "201", description = "Transaction evaluated")
    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
    @PostMapping
    public ResponseEntity<FraudResultResponse> submit(@Valid @RequestBody TransactionRequest request) {
        FraudResultResponse response = transactionService.submit(request);
        URI location = URI.create("/api/transactions/" + response.transactionId() + "/fraud-result");
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get fraud result",
            description = "Retrieves the fraud evaluation result for a previously submitted transaction.")
    @ApiResponse(responseCode = "200", description = "Fraud result found")
    @ApiResponse(responseCode = "404", description = "No result for this transaction", content = @Content)
    @GetMapping("/{id}/fraud-result")
    public FraudResultResponse getFraudResult(@PathVariable UUID id) {
        return transactionService.getResult(id);
    }
}
