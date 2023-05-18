package uk.gov.companieshouse.itemgroupworkflowapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.itemgroupworkflowapi.dto.TestDTO;
import uk.gov.companieshouse.itemgroupworkflowapi.logging.LoggingUtils;
import uk.gov.companieshouse.logging.util.DataMap;

@RestController
public class TestController {
    private static final String LOG_PREFIX = "<=TestController=>>";
    private final LoggingUtils logger;

    public TestController(LoggingUtils logger) {
        this.logger = logger;
    }

    @PostMapping("/dto_test")
    public ResponseEntity<Object> signPdf(final @RequestBody TestDTO theTestDTO) {
        boolean status = false;

        if (theTestDTO == null ||
            theTestDTO.getCompanyNumber().length() == 0 ||
            theTestDTO.getCompanyName().length() == 0) {
            logger.getLogger().error(LOG_PREFIX + " We got an empty DTO /o\\");
            return (ResponseEntity<Object>) ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }

        logDTO(LOG_PREFIX, theTestDTO);

        return(ResponseEntity<Object>) ResponseEntity.status(HttpStatus.CREATED);
    }

    public void logDTO(String logMessage, TestDTO theDTO) {

        var dataMap = new DataMap.Builder()
            .companyName(theDTO.getCompanyName())
            .companyNumber(theDTO.getCompanyNumber())
            .build();

        logger.getLogger().info(logMessage, dataMap.getLogMap());
    }
}
