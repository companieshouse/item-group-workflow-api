package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.APPLICATION_NAMESPACE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TestDTO;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.itemgroupworkflowapi.model.TestDtoItem;
import uk.gov.companieshouse.itemgroupworkflowapi.repository.ItemGroupsRepository;
import uk.gov.companieshouse.itemgroupworkflowapi.service.ItemGroupsService;
import uk.gov.companieshouse.logging.util.DataMap;

@RestController
public class SanityController {
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;

    @Autowired
    MongoTemplate mongoTemplate;

    public SanityController(LoggingUtils logger, ItemGroupsService itemGroupsService) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
    }

    @GetMapping("${root_controller.ok}")
    public ResponseEntity<String> get200response_returnAppName() {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 200");
        return(new ResponseEntity<>(APPLICATION_NAMESPACE, HttpStatus.OK));
    }

    @GetMapping("${root_controller.created}")
    public ResponseEntity<Void> get201response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 201");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("${root_controller.unauthorized}")
    public ResponseEntity<Void> get401response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("${root_controller.dto_test}")
    public ResponseEntity<Object> postDtoTest_returnDto(final @RequestBody TestDTO postDTO) {

        logger.getLogger().info("POST DTO = " + postDTO);
        final TestDTO savedDTO = itemGroupsService.saveTestDto(postDTO);
        logger.getLogger().info("SAVE DTO = " + savedDTO);

        return(ResponseEntity.status(HttpStatus.CREATED).body(savedDTO));
    }

    private void logMapWithMessage(String logMessage, TestDTO theDTO) {

        var dataMap = new DataMap.Builder()
            .companyName(theDTO.getCompanyName())
            .companyNumber(theDTO.getCompanyNumber())
            .build();

        logger.getLogger().info(logMessage, dataMap.getLogMap());
    }
}
