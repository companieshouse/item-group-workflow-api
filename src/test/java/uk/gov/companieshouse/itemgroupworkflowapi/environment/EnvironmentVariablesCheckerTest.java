package uk.gov.companieshouse.itemgroupworkflowapi.environment;

import org.junit.Rule;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.Arrays;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.companieshouse.itemgroupworkflowapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BOOTSTRAP_SERVER_URL;
import static uk.gov.companieshouse.itemgroupworkflowapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CHS_KAFKA_API_URL;
import static uk.gov.companieshouse.itemgroupworkflowapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.ITEM_ORDERED_CERTIFIED_COPY_TOPIC;
import static uk.gov.companieshouse.itemgroupworkflowapi.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.MONGODB_URL;

@SpringBootTest
@EmbeddedKafka
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @AfterEach
    void tearDown() {
        final String[] AllEnvironmentVariableNames =
                Arrays.stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.class.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new);
        environmentVariables.clear(AllEnvironmentVariableNames);
    }

    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(this::accept);
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertThat(allPresent, is(true));
    }

    @DisplayName("returns false if MONGODB_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfMongoDbUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(MONGODB_URL);
    }

    @DisplayName("returns false if BOOTSTRAP_SERVER_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBootstrapServerUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BOOTSTRAP_SERVER_URL);
    }

    @DisplayName("returns false if ITEM_ORDERED_CERTIFIED_COPY_TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfItemOrderedCertifiedCopyTopicMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(ITEM_ORDERED_CERTIFIED_COPY_TOPIC);
    }

    @DisplayName("returns false if CHS_KAFKA_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsKafkaApiUrlMissing() {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_KAFKA_API_URL);
    }

    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
            final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(variable -> {
            if (variable != excludedVariable) {
                environmentVariables.set(variable.getName(), TOKEN_VALUE);
            }
        });
        boolean allPresent = EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent();
        assertFalse(allPresent);
    }

    private void accept(EnvironmentVariablesChecker.RequiredEnvironmentVariables variable) {
        environmentVariables.set(variable.getName(), TOKEN_VALUE);
    }
}