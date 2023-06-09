package uk.gov.companieshouse.itemgroupworkflowapi.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.JsonMergePatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.itemgroupworkflowapi.controller.ApiErrors;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.ItemPatchValidationDto;
import uk.gov.companieshouse.itemgroupworkflowapi.util.FieldNameConverter;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TestMergePatchFactory;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests the {@link PatchItemRequestValidator} class.
 */
@SpringBootTest
class PatchItemRequestValidatorTest {

    @Configuration
    public static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public Validator validator() {
            final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            return factory.getValidator();
        }

        @Bean
        public FieldNameConverter converter() {
            return new FieldNameConverter();
        }

        @Bean
        public PatchItemRequestValidator patchItemRequestValidator() {
            return new PatchItemRequestValidator(objectMapper(), validator(), converter());
        }

        @Bean
        TestMergePatchFactory patchFactory() {
            return new TestMergePatchFactory(objectMapper());
        }

    }

    private static final String DIGITAL_DOCUMENT_LOCATION_URI =
            "s3://document-api-images-cidev/docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf";

    private static final String INVALID_DIGITAL_DOCUMENT_LOCATION_URI =
            "s3:// document-api-images-cidev/docs/-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf";

    @Autowired
    private PatchItemRequestValidator validatorUnderTest;

    @Autowired
    private TestMergePatchFactory patchFactory;

    private ItemPatchValidationDto itemUpdate;

    @BeforeEach
    void setUp() {
        itemUpdate = new ItemPatchValidationDto();
    }

    @Test
    @DisplayName("No errors raised for a valid patch")
    void getValidationErrorsReturnsNoErrors() throws IOException {
        // Given
        itemUpdate.setStatus("satisfied");
        itemUpdate.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION_URI);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, is(empty()));
    }

    @Test
    @DisplayName("Missing digital document location is not a problem")
    void getValidationErrorsReturnsNoErrorsForMissingDigitalDocumentLocation() throws IOException {
        // Given
        itemUpdate.setStatus("satisfied");
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, is(empty()));
    }

    @Test
    @DisplayName("Error raised for missing status")
    void getValidationErrorsReturnsMissingStatusError() throws IOException {
        // Given
        itemUpdate.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION_URI);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        expectError(errors, "status-error", "status: must not be null");
    }

    @Test
    @DisplayName("Error raised for empty status")
    void getValidationErrorsReturnsEmptyStatusError() throws IOException {
        // Given
        itemUpdate.setStatus("");
        itemUpdate.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION_URI);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        expectError(errors, "status-error", "status: must be one of [pending, processing, satisfied, cancelled, failed]");
    }

    @Test
    @DisplayName("Error raised for invalid status")
    void getValidationErrorsReturnsInvalidStatusError() throws IOException {
        // Given
        itemUpdate.setStatus("unknown");
        itemUpdate.setDigitalDocumentLocation(DIGITAL_DOCUMENT_LOCATION_URI);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        expectError(errors, "status-error", "status: must be one of [pending, processing, satisfied, cancelled, failed]");
    }

    @Test
    @DisplayName("Error raised for invalid digital document location")
    void getValidationErrorsReturnsInvalidDigitalDocumentLocationError() throws IOException {
        // Given
        itemUpdate.setStatus("satisfied");
        itemUpdate.setDigitalDocumentLocation(INVALID_DIGITAL_DOCUMENT_LOCATION_URI);
        final JsonMergePatch patch = patchFactory.patchFromPojo(itemUpdate);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        expectError(errors,
                "digital-document-location-error",
                "digital_document_location: s3:// document-api-images-cidev/docs/" +
                        "-fsWaC-ED30jRNACt2dqNYc-lH2uODjjLhliYjryjV0/application-pdf is not a valid URI.");
    }

    @Test
    @DisplayName("Validation error raised if unknown field specified")
    void getValidationErrorsRaisesErrorIfUnknownFieldSpecified() throws IOException {
        // Given
        final String jsonWithUnknownField = "{ \"unknown\": \"unknown\" }";
        final JsonMergePatch patch = patchFactory.patchFromJson(jsonWithUnknownField);

        // When
        final List<ApiError> errors = validatorUnderTest.getValidationErrors(patch);

        // Then
        assertThat(errors, contains(ApiErrors.ERR_JSON_PROCESSING));
    }

    private void expectError(final List<ApiError> errors, final String error, final String errorMessage) {
        assertThat(errors.size(), is(1));
        assertThat(errors.get(0).getError(), is(error));
        assertThat(errors.get(0).getErrorValues().get("error_message"), is(errorMessage));
    }

}
