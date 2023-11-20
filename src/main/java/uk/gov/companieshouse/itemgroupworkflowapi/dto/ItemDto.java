package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemDto {

    private final String id;

    private final String status;

    @JsonProperty("digital_document_location")
    private final String digitalDocumentLocation;

    public ItemDto(String id, String status, String digitalDocumentLocation) {
        this.id = id;
        this.status = status;
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getDigitalDocumentLocation() {
        return digitalDocumentLocation;
    }

}
