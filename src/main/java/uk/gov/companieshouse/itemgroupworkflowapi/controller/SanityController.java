package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import static uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtilsConfiguration.APPLICATION_NAMESPACE;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.TestDTO;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.util.DataMap;

@RestController
public class SanityController {
    public static final String OK_URI           = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.ok}";
    public static final String CREATED_URI      = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.created}";
    public static final String UNAUTHORIZED_URI = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.unauthorized}";
    public static final String DTO_TEST_URI     = "${uk.gov.companieshouse.itemgroupworkflowapi.root_controller.dto_test}";
    private static final String LOG_PREFIX = "<=SanityController=>";
    private final LoggingUtils logger;

    public SanityController(LoggingUtils logger) {
        this.logger = logger;
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

        logMapWithMessage(LOG_PREFIX, theTestDTO);

        return(ResponseEntity.status(HttpStatus.CREATED).body(theTestDTO));
    }

    public void logMapWithMessage(String logMessage, TestDTO theDTO) {

        var dataMap = new DataMap.Builder()
            .companyName(theDTO.getCompanyName())
            .companyNumber(theDTO.getCompanyNumber())
            .build();

        logger.getLogger().info(logMessage, dataMap.getLogMap());
    }
}
