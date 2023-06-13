package uk.gov.companieshouse.itemgroupworkflowapi.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemLinks;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Unit tests the {@link LinksGeneratorService} class.
 */
class LinksGeneratorServiceTest {

    private static final String SELF_PATH = "/item_groups";
    private static final String ORDER_PATH = "/orders/ORD-065216-517934";
    private static final String ORIGINAL_ITEM = "/orderable/certified-copies/CCD-768116-517930";
    private static final String ITEM_GROUP_ID = "IG-268916-863247";
    private static final String ITEM_ID = "CCD-768116-517930";

    private LinksGeneratorService generatorUnderTest;

    @BeforeEach
    void setUp() {
        generatorUnderTest = new LinksGeneratorService(SELF_PATH);
    }

    @Test
    @DisplayName("Regenerates all links correctly")
    void regeneratesAllLinksCorrectly() {

        // Given
        final var data = createIncomingData();

        // When
        generatorUnderTest.regenerateLinks(data, ITEM_GROUP_ID);

        // Then
        assertThat(data.getLinks().getOrder(), is(ORDER_PATH));
        assertThat(data.getLinks().getSelf(), is(SELF_PATH + "/" + ITEM_GROUP_ID));

        assertThat(data.getItems().get(0).getLinks().getOriginalItem(), is(ORIGINAL_ITEM));
        assertThat(data.getItems().get(0).getLinks().getSelf(),
                is(SELF_PATH + "/" + ITEM_GROUP_ID + "/items/" + ITEM_ID));

        assertThat(data.getItems().get(1).getLinks().getOriginalItem(), is(ORIGINAL_ITEM));
        assertThat(data.getItems().get(1).getLinks().getSelf(),
                is(SELF_PATH + "/" + ITEM_GROUP_ID + "/items/" + ITEM_ID));
    }


    @Test
    @DisplayName("Generates group links correctly with valid inputs")
    void generatesGroupLinksCorrectlyWithValidInputs() {

        // When
        final Links links = generatorUnderTest.generateItemGroupLinks(ORDER_PATH, ITEM_GROUP_ID);

        // Then
        assertThat(links.getSelf(), is(SELF_PATH + "/" + ITEM_GROUP_ID));
    }

    @Test
    @DisplayName("Generates item links correctly with valid inputs")
    void generatesItemLinksCorrectlyWithValidInputs() {

        // When
        final ItemLinks links = generatorUnderTest.generateItemLinks(ORIGINAL_ITEM, ITEM_GROUP_ID, ITEM_ID);

        // Then
        assertThat(links.getSelf(), is(SELF_PATH + "/" + ITEM_GROUP_ID + "/items/" + ITEM_ID));
    }

    @Test
    @DisplayName("Unpopulated item group ID argument results in an IllegalArgumentException for group links")
    void itemGroupIdMustNotBeBlankForGroupLinks() {

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateItemGroupLinks(ORDER_PATH, null));

        // Then
        assertThat(exception.getMessage(), is("Item Group ID not populated!"));
    }

    @Test
    @DisplayName("Unpopulated item group ID argument results in an IllegalArgumentException for item links")
    void itemGroupIdMustNotBeBlankForItemLinks() {

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateItemLinks(ORIGINAL_ITEM, null, ITEM_ID));

        // Then
        assertThat(exception.getMessage(), is("Item Group ID not populated!"));
    }

    @Test
    @DisplayName("Unpopulated item ID argument results in an IllegalArgumentException for item links")
    void itemIdMustNotBeBlankForItemLinks() {

        // When and then
        final IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                        () -> generatorUnderTest.generateItemLinks(ORIGINAL_ITEM, ITEM_GROUP_ID, null));

        // Then
        assertThat(exception.getMessage(), is("Item ID not populated!"));
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

    private ItemGroupData createIncomingData() {
        final var data = new ItemGroupData();
        final var links = new Links();
        links.setOrder(ORDER_PATH);
        data.setLinks(links);

        final var item = new Item();
        item.setId(ITEM_ID);
        final var itemLinks = new ItemLinks();
        itemLinks.setOriginalItem(ORIGINAL_ITEM);
        item.setLinks(itemLinks);

        data.setItems(List.of(item, item));
        return data;
    }

}
