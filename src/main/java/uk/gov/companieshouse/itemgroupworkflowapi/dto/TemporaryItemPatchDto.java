package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.Status;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.Uri;
import uk.gov.companieshouse.itemgroupworkflowapi.validator.ValueOfEnum;

import javax.validation.constraints.NotNull;

/**
 * TODO DCAC-78 Remove this.
 * Temporary aid to merging for an item implemented as a Map.
 */
public class TemporaryItemPatchDto {

    @JsonProperty("digital_document_location")
    @Uri
    private String digitalDocumentLocation;

    @JsonProperty("status")
    @NotNull
    @ValueOfEnum(enumClass = Status.class)
    private String status;

    public String getDigitalDocumentLocation() {
        return digitalDocumentLocation;
    }

    public void setDigitalDocumentLocation(String digitalDocumentLocation) {
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
