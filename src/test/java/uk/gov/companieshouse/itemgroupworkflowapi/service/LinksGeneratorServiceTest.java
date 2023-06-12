package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests the {@link LinksGeneratorService} class.
 */
class LinksGeneratorServiceTest {

    private static final String SELF_PATH = "/item_groups";
    private static final String ORDER_PATH = "/orders/ORD-065216-517934";
    private static final String ITEM_GROUP_ID = "IG-268916-863247";

    @Test
    @DisplayName("Generates links correctly with valid inputs")
    void generatesLinksCorrectlyWithValidInputs() {

        // Given
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(SELF_PATH);

        // When
        final Links links = generatorUnderTest.generateItemGroupLinks(ORDER_PATH, ITEM_GROUP_ID);

        // Then
        assertThat(links.getSelf(), is(SELF_PATH + "/" + ITEM_GROUP_ID));
    }

    @Test
    @DisplayName("Unpopulated item group ID argument results in an IllegalArgumentException")
    void itemGroupIdMustNotBeBlank() {
        // Given
        final LinksGeneratorService generatorUnderTest = new LinksGeneratorService(SELF_PATH);

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateItemGroupLinks(ORDER_PATH, null));

        // Then
        assertThat(exception.getMessage(), is("Item Group ID not populated!"));
    }

    @Test
    @DisplayName("Unpopulated path to self URI results in an IllegalArgumentException")
    void selfPathMustNotBeBlank() {

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> new LinksGeneratorService(null));

        // Then
        assertThat(exception.getMessage(), is("Path to self URI not configured!"));
    }

}
