package uk.gov.companieshouse.item.group.workflow.api.controller;

import static uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtilsConfiguration.APPLICATION_NAMESPACE;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroup;
import uk.gov.companieshouse.item.group.workflow.api.model.ItemGroupJSON;
import uk.gov.companieshouse.item.group.workflow.api.logging.LoggingUtils;
import uk.gov.companieshouse.item.group.workflow.api.service.ItemGroupsService;
import uk.gov.companieshouse.logging.util.DataMap;

@RestController
public class SanityController {
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;

    public SanityController(LoggingUtils logger, ItemGroupsService itemGroupsService) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
    }

    @GetMapping("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.ok}")
    public ResponseEntity<String> get200response_returnAppName() {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 200");
        return(new ResponseEntity<>(APPLICATION_NAMESPACE, HttpStatus.OK));
    }

    @GetMapping("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.created}")
    public ResponseEntity<Void> get201response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 201");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.unauthorized}")
    public ResponseEntity<Void> get401response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("${uk.gov.companieshouse.item.group.workflow.api.sanity.controller.dto_test}")
    public ResponseEntity<Object> postDtoTest_returnDto(final @RequestBody ItemGroupJSON itemGroupCreateJSON) {

        logger.getLogger().info("POST payload = " + itemGroupCreateJSON);
        final ItemGroup savedItem = itemGroupsService.CreateItemGroup(itemGroupCreateJSON);
        logger.getLogger().info("SAVE ItemGroup = " + savedItem);

        return(ResponseEntity.status(HttpStatus.CREATED).body(savedItem));
    }
//
//    private void logMapWithMessage(String logMessage, TestDTO theDTO) {
//
//        var dataMap = new DataMap.Builder()
//            .companyName(theDTO.getCompanyName())
//            .companyNumber(theDTO.getCompanyNumber())
//            .build();
//
//        logger.getLogger().info(logMessage, dataMap.getLogMap());
//    }
}
