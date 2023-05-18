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
public class TestController {
    private static final String LOG_PREFIX = "<=TestController=>";
    private final LoggingUtils logger;

    public TestController(LoggingUtils logger) {
        this.logger = logger;
    }

    @GetMapping("/")
    public ResponseEntity<String> rootCheck () {

        logger.getLogger().debug(APPLICATION_NAMESPACE + " => rootCheck + 200");

        return(new ResponseEntity<>(APPLICATION_NAMESPACE, HttpStatus.OK));
    }

    @GetMapping("/created")
    public ResponseEntity<Void> get201response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 201");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/unauthorized")
    public ResponseEntity<Void> get401response () {
        logger.getLogger().debug(APPLICATION_NAMESPACE + " => 401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/dto_test")
    public ResponseEntity<Object> dtoTestPost (final @RequestBody TestDTO theTestDTO) {

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
