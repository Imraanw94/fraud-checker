package com.imraan.fraudcheck.boundary;

import com.jayway.jsonpath.JsonPath;
import com.imraan.fraudcheck.TestcontainersConfiguration;
import com.imraan.fraudcheck.entity.enums.FraudDecision;
import com.imraan.fraudcheck.entity.FraudResult;
import com.imraan.fraudcheck.entity.Transaction;
import com.imraan.fraudcheck.entity.repository.FraudResultRepository;
import com.imraan.fraudcheck.entity.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the full stack (controller, service, evaluator, repositories, Flyway-migrated PostgreSQL
 * running in a Testcontainer) through the REST API.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TransactionApiIntegrationTest {

    private static final String TRANSACTIONS = "/api/transactions";

    @Autowired
    private WebApplicationContext context;
    @Value("${security.api.username}")
    private String apiUsername;
    @Value("${security.api.password}")
    private String apiPassword;
    private MockMvc mockMvc;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private FraudResultRepository fraudResultRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())
                .defaultRequest(get("/").with(httpBasic(apiUsername, apiPassword))).build();
        fraudResultRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    private static String body(String accountId, String amount, String currency, String merchant, String country) {
        return "{\"accountId\": %s, \"amount\": %s, \"currency\": %s, \"merchant\": %s, \"country\": %s}"
                .formatted(accountId, amount, currency, merchant, country);
    }

    private static String cleanBody() {
        return body("\"ACC-IT-1\"", "250.00", "\"zar\"", "\"Acme Store\"", "\"ZA\"");
    }

    private MvcResult submit(String json) throws Exception {
        return mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID transactionId(MvcResult result) throws Exception {
        return UUID.fromString(JsonPath.read(result.getResponse().getContentAsString(), "$.transactionId"));
    }

    @Test
    void submitApprovedTransactionReturns201WithResult() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON).content(cleanBody()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/fraud-result")))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.decision").value("APPROVED"))
                .andExpect(jsonPath("$.riskScore").value(0))
                .andExpect(jsonPath("$.triggeredRules", empty()))
                .andExpect(jsonPath("$.evaluatedAt").exists());
    }

    @Test
    void submitReviewTransactionTriggersRule() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"ACC-IT-2\"", "50.00", "\"ZAR\"", "\"Acme Store\"", "\"IL\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision").value("REVIEW"))
                .andExpect(jsonPath("$.riskScore").value(50))
                .andExpect(jsonPath("$.triggeredRules", containsInAnyOrder("HIGH_RISK_COUNTRY")));
    }

    @Test
    void submitRejectedTransactionTriggersMultipleRules() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"ACC-IT-3\"", "20000.00", "\"ZAR\"", "\"ShadyMerchant\"", "\"ZA\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.decision").value("REJECTED"))
                .andExpect(jsonPath("$.riskScore").value(90))
                .andExpect(jsonPath("$.triggeredRules", containsInAnyOrder("HIGH_AMOUNT", "BLACKLISTED_MERCHANT")));
    }

    @Test
    void submitPersistsTransactionAndFraudResult() throws Exception {
        UUID transactionId = transactionId(submit(
                body("\"ACC-IT-4\"", "50.00", "\"zar\"", "\"Acme Store\"", "\"IL\"")));

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        assertThat(transaction.getAccountId()).isEqualTo("ACC-IT-4");
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(transaction.getCurrency()).isEqualTo("ZAR");
        assertThat(transaction.getMerchant()).isEqualTo("Acme Store");
        assertThat(transaction.getCountry()).isEqualTo("IL");
        assertThat(transaction.getCreatedAt()).isNotNull();

        FraudResult result = fraudResultRepository.findByTransactionId(transactionId).orElseThrow();
        assertThat(result.getDecision()).isEqualTo(FraudDecision.REVIEW);
        assertThat(result.getRiskScore()).isEqualTo(50);
        assertThat(result.getEvaluatedAt()).isNotNull();

        // triggeredRules is lazy and there is no session here, so verify it directly in PostgreSQL.
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from transactions where id = ?", Long.class, transactionId)).isEqualTo(1L);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from fraud_results where transaction_id = ?", Long.class, transactionId))
                .isEqualTo(1L);
        assertThat(jdbcTemplate.queryForList(
                "select rule_code from fraud_result_triggered_rules where fraud_result_id = ?",
                String.class, result.getId())).containsExactly("HIGH_RISK_COUNTRY");
    }

    @Test
    void getFraudResultReturnsStoredResult() throws Exception {
        MvcResult submitted = submit(body("\"ACC-IT-5\"", "50.00", "\"ZAR\"", "\"Acme Store\"", "\"IL\""));
        UUID transactionId = transactionId(submitted);
        String location = submitted.getResponse().getHeader("Location");
        String createdId = JsonPath.read(submitted.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.decision").value("REVIEW"))
                .andExpect(jsonPath("$.riskScore").value(50))
                .andExpect(jsonPath("$.triggeredRules", containsInAnyOrder("HIGH_RISK_COUNTRY")))
                .andExpect(jsonPath("$.evaluatedAt").exists());
    }

    // Error Scenarios
    @Test
    void getFraudResultForUnknownTransactionReturns404() throws Exception {
        UUID unknown = UUID.randomUUID();

        mockMvc.perform(get(TRANSACTIONS + "/{id}/fraud-result", unknown))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No fraud result for transaction " + unknown));
    }

    @Test
    void unknownEndpointReturns404() throws Exception {
        mockMvc.perform(get("/api/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void invalidFieldsReturn400WithViolationsAndPersistNothing() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON)
                        .content(body("\"\"", "-1", "\"US\"", "null", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.violations[?(@.field=='accountId')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field=='amount')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field=='currency')]").exists());

        assertThat(transactionRepository.count()).isZero();
        assertThat(fraudResultRepository.count()).isZero();
    }

    @Test
    void missingRequiredFieldsReturn400() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post(TRANSACTIONS).contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));

        assertThat(transactionRepository.count()).isZero();
    }

    @Test
    void invalidTransactionIdReturns400() throws Exception {
        mockMvc.perform(get(TRANSACTIONS + "/not-a-uuid/fraud-result"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("id"));
    }
}
