package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemDto {

    private String id;

    private String status; // TODO DCAC-241 use enum? (relocate)

    @JsonProperty("digital_document_location")
    private String digitalDocumentLocation;

    public ItemDto(String id, String status, String digitalDocumentLocation) {
        this.id = id;
        this.status = status;
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDigitalDocumentLocation() {
        return digitalDocumentLocation;
    }

    public void setDigitalDocumentLocation(String digitalDocumentLocation) {
        this.digitalDocumentLocation = digitalDocumentLocation;
    }
}
