package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemDto(String id, String status,
                      @JsonProperty("digital_document_location") String digitalDocumentLocation) {

    public ItemDto(String id, String status, String digitalDocumentLocation) {
        this.id = id;
        this.status = status;
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    @Override
    public String digitalDocumentLocation() {
        return digitalDocumentLocation;
    }

}
