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
    public static final String OK_URI           = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.ok}";
    public static final String CREATED_URI      = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.created}";
    public static final String UNAUTHORIZED_URI = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.unauthorized}";
    public static final String DTO_TEST_URI     = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.dto_test}";
    public static final String MONGO_CHECK_URI = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.mongo_check}";
    @Value("${uk.gov.companieshouse.itemgroupworkflowapi.mongo.url}")
    private String mongoDbConnectionStr;
    @Value("${uk.gov.companieshouse.itemgroupworkflowapi.database_name}")
    private String databaseName;
    @Value("${uk.gov.companieshouse.itemgroupworkflowapi.collection_name}")
    private String collectionName;
    private static final String LOG_PREFIX = "<=SanityController=>";
    private final LoggingUtils logger;
    private final ItemGroupsService itemGroupsService;
    private final ItemGroupsRepository itemGroupsRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    public SanityController(LoggingUtils logger, ItemGroupsService itemGroupsService, ItemGroupsRepository itemGroupsRepository) {
        this.logger = logger;
        this.itemGroupsService = itemGroupsService;
        this.itemGroupsRepository = itemGroupsRepository;
    }

    @GetMapping(OK_URI)
    public ResponseEntity<String> get200response_returnAppName() {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 200");
        return(new ResponseEntity<>(APPLICATION_NAMESPACE, HttpStatus.OK));
    }

    @GetMapping(CREATED_URI)
    public ResponseEntity<Void> get201response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 201");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(UNAUTHORIZED_URI)
    public ResponseEntity<Void> get401response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping(DTO_TEST_URI)
    public ResponseEntity<Object> postDtoTest_returnDto(final @RequestBody TestDTO theTestDTO) {

        logMapWithMessage("POST got DTO => ", theTestDTO);

//        final TestDTO savedTestDto = itemGroupsRepository.save((TestDtoItem)theTestDTO);
//        final TestDTO savedTestDto = itemGroupsRepository.save(theTestDTO);
        final TestDTO savedTestDto = itemGroupsRepository.insert(theTestDTO);

        logger.getLogger().info("Repo has " + Long.toString(itemGroupsRepository.count()) + " records");

        savedTestDto.setCompanyName("Slimelight");
        logMapWithMessage("After SAVE returned DTO => ", savedTestDto);

        return(ResponseEntity.status(HttpStatus.CREATED).body(savedTestDto));
    }

    public void logMapWithMessage(String logMessage, TestDTO theDTO) {

        var dataMap = new DataMap.Builder()
            .companyName(theDTO.getCompanyName())
            .companyNumber(theDTO.getCompanyNumber())
            .build();

        logger.getLogger().info(logMessage, dataMap.getLogMap());
    }

    @GetMapping(MONGO_CHECK_URI)
    public ResponseEntity<String> getMongoResponse () {
//        StringBuilder message = new StringBuilder()
//            .append(APPLICATION_NAMESPACE)
//            .append(" => Mongo collection item_groups ")
//            .append((mongoFound ? "FOUND \\o/" : "NOT FOUND /o\\"));
//
//        logger.getLogger().info(message.toString());
//
//        return(new ResponseEntity<>(message.toString(), HttpStatus.OK));
        return(new ResponseEntity<>("blah blah...", HttpStatus.OK));
    }
}
