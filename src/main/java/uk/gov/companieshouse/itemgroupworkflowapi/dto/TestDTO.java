package uk.gov.companieshouse.itemgroupworkflowapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestDTO {
    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public String toString() {
        return "TestDTO{" +
            "companyName='" + companyName + '\'' +
            ", companyNumber='" + companyNumber + '}';
    }
}
