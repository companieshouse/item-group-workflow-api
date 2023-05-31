package uk.gov.companieshouse.item.group.workflow.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemGroupJSON {
    //
    // company_number
    //
    @JsonProperty("company_number")
    private String companyNumber;
    public String getCompanyNumber() {
        return companyNumber;
    }
    //
    // company_name
    //
    @JsonProperty("company_name")
    private String companyName;
    public String getCompanyName() {
        return companyName;
    }

    @Override
    public String toString() {
        return "TestDTO {" +
            "company_number='" + companyNumber + '\'' +
            ", company_name='" + companyName + '}';
    }
}