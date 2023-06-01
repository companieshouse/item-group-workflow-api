package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import javax.validation.constraints.NotEmpty;
import java.net.URI;

/**
 * Instantiated from PATCH request JSON body to facilitate PATCH request validation.
 */
public class ItemPatchValidationDto {

    @JsonProperty("digital_document_location")
    private URI digitalDocumentLocation;

    @JsonProperty("status")
    @NotEmpty
    private String status;

    public void setDigitalDocumentLocation(URI digitalDocumentLocation) {
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
