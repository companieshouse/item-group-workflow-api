package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService.MONGO_EXISTS_EXCEPTION_MESSAGE;
import static uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService.MONGO_SAVE_EXCEPTION_MESSAGE;
import com.mongodb.MongoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.itemgroupworkflowapi.exception.MongoOperationException;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.DeliveryDetails;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Item;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCostProductType;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemCosts;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemDescriptionIdentifier;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroup;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemGroupData;
import uk.gov.companieshouse.itemgroupworkflowapi.model.ItemKind;
import uk.gov.companieshouse.itemgroupworkflowapi.model.Links;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.itemgroupworkflowapi.validation.ItemGroupsValidator;
import uk.gov.companieshouse.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ItemGroupControllerTest {
    private static final String X_REQUEST_ID = "12345";
    private static final String VALID_ORDER_NUMBER = "lucky string";
    private static final String VALID_DELIVERY_COMPANY_NAME = "Delivery Test Company";
    private static final String VALID_ITEM_COMPANY_NAME = "Item Test Company";
    private static final String VALID_COMPANY_NUMBER = "IG-12345-67890";
    private static final String EXAMPLE_ERROR_MESSAGE = "Example error message";

    @InjectMocks
    ItemGroupController controller;

    @Mock
    private LoggingUtils loggingUtils;

    @Mock
    private Logger logger;

    @Mock
    private ItemGroupsValidator requestValidator;

    @Mock
    private ItemGroupsService itemGroupsService;

    @Test
    @DisplayName("create item group succeeds return 201 CREATED")
    void createItemGroupReturnCreated201()  throws Exception {
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);

        when(itemGroupsService.createItemGroup(itemGroupData)).thenReturn(itemGroupData);
        when(loggingUtils.getLogger()).thenReturn(logger);
        //
        // Verify HttpStatus.CREATED returned.
        //
        final ResponseEntity<Object> response = controller.createItemGroup(X_REQUEST_ID, itemGroupData);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        //
        // ItemGroup
        //
        final ItemGroupData createdItemGroupData = (ItemGroupData) response.getBody();
        assertNotNull(createdItemGroupData);
        assertThat(createdItemGroupData.getOrderNumber(), is(VALID_ORDER_NUMBER));

        assertThat(createdItemGroupData.getOrderNumber(), is(VALID_ORDER_NUMBER));
        assertThat(createdItemGroupData.getDeliveryDetails().getCompanyName(), is(VALID_DELIVERY_COMPANY_NAME));
        assertThat(createdItemGroupData.getLinks().getOrder(), is(VALID_ORDER_NUMBER));
        //
        // Item
        //
        assertEquals(1, createdItemGroupData.getItems().size());
        Item item = createdItemGroupData.getItems().get(0);
        assertNotNull(item);

        assertThat(item.getCompanyNumber(), is(VALID_COMPANY_NUMBER));
        assertThat(item.getCompanyName(), is(VALID_ITEM_COMPANY_NAME));
        assertThat(item.getDescriptionIdentifier(), is(ItemDescriptionIdentifier.CERTIFIED_COPY.toString()));
        assertThat(item.getKind(), is(ItemKind.ITEM_CERTIFIED_COPY.toString()));
        //
        // ItemCosts
        //
        assertEquals(1, item.getItemCosts().size());
        ItemCosts itemCosts = item.getItemCosts().get(0);
        assertNotNull(itemCosts);
        assertThat(itemCosts.getProductType(), is(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString()));
    }

    @Test
    @DisplayName("validation fails return 400 BAD_REQUEST")
    void validationFailsReturnBadRequest400()  throws Exception {
        final ItemGroupData itemGroupData = invalidItemGroupsDtoMissingAllValidationCriteria();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);

        when(requestValidator.validateCreateItemData(itemGroupData)).thenReturn(Arrays.asList(EXAMPLE_ERROR_MESSAGE));
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ResponseEntity<Object> response = controller.createItemGroup(X_REQUEST_ID, itemGroupData);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        List<String> errors = (List<String>) response.getBody();
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertThat(errors.get(0), is(EXAMPLE_ERROR_MESSAGE));
    }

    @Test
    @DisplayName("item group already exists return 409 CONFLICT")
    void itemGroupAlreadyExistsReturnConflict409()  throws Exception {
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);

        when(itemGroupsService.doesItemGroupExist(itemGroupData)).thenReturn(true);
        when(loggingUtils.getLogger()).thenReturn(logger);

        final ResponseEntity<Object> response = controller.createItemGroup(X_REQUEST_ID, itemGroupData);

        assertThat(response.getStatusCode(), is(HttpStatus.CONFLICT));

        ItemGroupData returnedItemGroupData = (ItemGroupData) response.getBody();
        assertNotNull(returnedItemGroupData);
        assertThat(returnedItemGroupData, is(itemGroupData));
    }

    @Test
    @DisplayName("Mongo EXISTS operation return 500 INTERNAL_SERVER_ERROR")
    void mongoExistsReturnInternalServerError500()  throws Exception {
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);
        //
        // Mongo EXISTS operation throws MongoOperationException.
        //
        final MongoOperationException moe = new MongoOperationException(
            MONGO_EXISTS_EXCEPTION_MESSAGE + itemGroupData.getOrderNumber(),
            new MongoException(""));
        when(itemGroupsService.doesItemGroupExist(itemGroupData)).thenThrow(moe);

        when(loggingUtils.getLogger()).thenReturn(logger);
        //
        // Verify HttpStatus.INTERNAL_SERVER_ERROR returned.
        //
        final ResponseEntity<Object> response = controller.createItemGroup(X_REQUEST_ID, itemGroupData);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

        String errorMessage = (String) response.getBody();
        assertNotNull(errorMessage);
        assert(errorMessage.contains(MONGO_EXISTS_EXCEPTION_MESSAGE));
        assert(errorMessage.contains(itemGroupData.getOrderNumber()));
    }

    @Test
    @DisplayName("Mongo SAVE operation return 500 INTERNAL_SERVER_ERROR")
    void mongoSaveReturnInternalServerError500()  throws Exception {
        final ItemGroupData itemGroupData = fairWeatherItemGroupsDto();
        ItemGroup itemGroup = new ItemGroup();
        itemGroup.setData(itemGroupData);
        //
        // Mongo SAVE operation throws MongoOperationException.
        //
        final MongoOperationException moe = new MongoOperationException(
            MONGO_SAVE_EXCEPTION_MESSAGE + itemGroupData.getOrderNumber(),
            new MongoException(""));
        when(itemGroupsService.createItemGroup(itemGroupData)).thenThrow(moe);

        when(loggingUtils.getLogger()).thenReturn(logger);
        //
        // Verify HttpStatus.INTERNAL_SERVER_ERROR returned.
        //
        final ResponseEntity<Object> response = controller.createItemGroup(X_REQUEST_ID, itemGroupData);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));

        String errorMessage = (String) response.getBody();
        assertNotNull(errorMessage);
        assert(errorMessage.contains(MONGO_SAVE_EXCEPTION_MESSAGE));
        assert(errorMessage.contains(itemGroupData.getOrderNumber()));
    }
    /**
     * DTO which will trigger all validation checks and pass
     */
    private ItemGroupData fairWeatherItemGroupsDto() {
        final ItemGroupData dto = new ItemGroupData();
        dto.setOrderNumber(VALID_ORDER_NUMBER);

        DeliveryDetails deliveryDetails = new DeliveryDetails();
        deliveryDetails.setCompanyName(VALID_DELIVERY_COMPANY_NAME);
        dto.setDeliveryDetails(deliveryDetails);

        ItemCosts itemCost = new ItemCosts();
        itemCost.setProductType(ItemCostProductType.CERTIFIED_COPY_INCORPORATION.toString());

        List<ItemCosts> itemCosts = new ArrayList<>();
        itemCosts.add(itemCost);

        Links links = new Links();
        links.setOrder(VALID_ORDER_NUMBER);
        dto.setLinks(links);

        Item item = new Item();
        item.setCompanyNumber(VALID_COMPANY_NUMBER);
        item.setCompanyName(VALID_ITEM_COMPANY_NAME);
        item.setDescriptionIdentifier(ItemDescriptionIdentifier.CERTIFIED_COPY.toString());
        item.setKind(ItemKind.ITEM_CERTIFIED_COPY.toString());
        item.setItemCosts(itemCosts);

        List<Item> items = new ArrayList<>();
        items.add(item);
        dto.setItems(items);

        return dto;
    }
    /**
     * Invalid DTO. No validation criteria met.
     */
    private ItemGroupData invalidItemGroupsDtoMissingAllValidationCriteria() {
        final ItemGroupData dto = new ItemGroupData();

        return dto;
    }
}