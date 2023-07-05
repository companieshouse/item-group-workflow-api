package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests the {@link IdGenerator} class.
 */
class IdGeneratorTest {

    @Test
    @DisplayName("generateId generates an ID of format IG-######-######")
    void generateIdGeneratesIdInCorrectFormat() {

        final var idGenerator = new IdGenerator();

        final String id = idGenerator.generateId();

        assertThat(id.matches("^IG-\\d{6}-\\d{6}$"), is(true));
    }

}
