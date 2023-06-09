package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.itemgroupworkflowapi.model.*;

import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.itemgroupworkflowapi.util.TestConstants;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests the {@link ItemGroupController} class.
 */
@ExtendWith(MockitoExtension.class)
class ItemGroupControllerTest {
    
    @Mock
    private Logger logger;

    @Mock
    private ItemGroupsRepository itemGroupsRepository;

    @Test
    @DisplayName("Create ItemGroup is successful")
    void createItemGroupSuccessfulwith201Created() {

        final LoggingUtils logger = new LoggingUtils(this.logger);
        final ItemGroupsService service = new ItemGroupsService(logger, itemGroupsRepository);
        final ItemGroupsValidator validator = new ItemGroupsValidator();
        final ItemGroupController controllerUnderTest = new ItemGroupController(logger, service, validator);

        final ItemGroupData itemGroupData = generateValidItemGroupData();
        final ItemGroup itemGroup = generateItemGroup();
        itemGroup.setData(itemGroupData);

        final ResponseEntity<Object> response = controllerUnderTest.createItemGroup("123", itemGroupData);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

    }

    @Test
    @DisplayName("Create ItemGroup is unsuccessful")
    void createItemGroupUnsuccessfulwith400BadRequest()  {

        // TO-DO IN HERE

//        ResponseEntity<Object> response =
//                controller.createItemGroup(TestConstants.TOKEN_REQUEST_ID_VALUE, itemGroupData);
//
//        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    /**
     * Item Group Data generation
     */
    private ItemGroupData generateValidItemGroupData() {
        final ItemGroupData itemGroupData = new ItemGroupData();
        itemGroupData.setOrderNumber("123");

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName("Test Company Name");
        itemGroupData.setDeliveryDetails(deliveryDetails);

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType("certificate");

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Links links = new Links();
        links.setOrder("123");
        itemGroupData.setLinks(links);

        Item item = new Item();
        item.setCompanyNumber("123");
        item.setCompanyName("Test Company Name");
        item.setDescriptionIdentifier("description");
        item.setKind("certificate");
        item.setItemCosts(itemCosts);

        List<Item> items = new ArrayList<>();
        items.add(item);
        itemGroupData.setItems(items);

        return itemGroupData;
    }

    /**
     * ItemGroup generation
     */

    private ItemGroup generateItemGroup() {
        final ItemGroup itemGroup = new ItemGroup();
        itemGroup.setId("123456");
        return itemGroup;
    }

}


