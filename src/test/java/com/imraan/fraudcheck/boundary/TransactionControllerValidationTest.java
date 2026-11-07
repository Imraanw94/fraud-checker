package com.imraan.fraudcheck.boundary;

import com.imraan.fraudcheck.control.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerValidationTest {

    private TransactionService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(TransactionService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(service))
                .setValidator(validator)
                .build();
    }

    private static String body(String accountId, String amount, String currency) {
        return "{\"accountId\": %s, \"amount\": %s, \"currency\": %s}".formatted(accountId, amount, currency);
    }

    private void assertRejected(String json) throws Exception {
        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "\"\"", "\" \""})
    void rejectsMissingOrBlankAccountId(String accountId) throws Exception {
        assertRejected(body(accountId, "10.00", "\"USD\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "\"\"", "\"US\"", "\"USDX\"", "\"U1D\""})
    void rejectsInvalidCurrency(String currency) throws Exception {
        assertRejected(body("\"ACC-1\"", "10.00", currency));
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "0", "0.00", "-1", "-0.01"})
    void rejectsMissingOrNonPositiveAmount(String amount) throws Exception {
        assertRejected(body("\"ACC-1\"", amount, "\"USD\""));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        assertRejected("{not json");
    }

    @Test
    void rejectsNonNumericAmount() throws Exception {
        assertRejected(body("\"ACC-1\"", "\"abc\"", "\"USD\""));
    }

    @Test
    void rejectsInvalidTransactionIdPathVariable() throws Exception {
        mockMvc.perform(get("/api/transactions/not-a-uuid/fraud-result"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
