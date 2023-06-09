package uk.gov.companieshouse.itemgroupworkflowapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.companieshouse.itemgroupworkflowapi.config.ApplicationConfiguration;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.itemgroupworkflowapi.validator.Status.PENDING;
import static uk.gov.companieshouse.itemgroupworkflowapi.validator.Status.SATISFIED;


/**
 * Unit tests the {@link PatchMerger} class.
 */
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig
class PatchMergerTest {

    @TestConfiguration
    static class Config {
        @Bean
        public ObjectMapper objectMapper() {
            return new ApplicationConfiguration().objectMapper();
        }

        @Bean
        PatchMerger patchMerger() {
            return new PatchMerger(objectMapper());
        }

        @Bean
        TestMergePatchFactory patchFactory() {
            return new TestMergePatchFactory(objectMapper());
        }
    }

    private static final String ORIGINAL_STATUS = PENDING.toString();
    private static final String PATCHED_STATUS = SATISFIED.toString();
    private static final String ORIGINAL_DOCUMENT_LOCATION = null;
    private static final String PATCHED_DOCUMENT_LOCATION =
            "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";

    private static final Item ORIGINAL_ITEM;
    private static final Item PATCHED_ITEM;
    static {
        ORIGINAL_ITEM = new Item();
        ORIGINAL_ITEM.setStatus(ORIGINAL_STATUS);
        ORIGINAL_ITEM.setDigitalDocumentLocation(ORIGINAL_DOCUMENT_LOCATION);
        PATCHED_ITEM = new Item();
        PATCHED_ITEM.setStatus(PATCHED_STATUS);
        PATCHED_ITEM.setDigitalDocumentLocation(PATCHED_DOCUMENT_LOCATION);
    }
    private static final Item EMPTY = new Item();

    @Autowired
    private PatchMerger patchMergerUnderTest;

    @Autowired
    private TestMergePatchFactory patchFactory;

    @Test
    @DisplayName("Unpopulated patch fields leave item unchanged")
    void unpopulatedPatchFieldsLeaveItemUnchanged() throws IOException {

        // When
        final var patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromPojo(EMPTY), ORIGINAL_ITEM, Item.class);

        // Then
        assertThat(patched, is(ORIGINAL_ITEM));
    }

    @Test
    @DisplayName("Populated patch fields overwrite item fields")
    void populatedPatchFieldsOverwriteItemFields() throws IOException {

        // When
        final var patched =
                patchMergerUnderTest.mergePatch(patchFactory.patchFromPojo(PATCHED_ITEM), ORIGINAL_ITEM, Item.class);

        // Then
        assertThat(patched, is(PATCHED_ITEM));
    }

}
