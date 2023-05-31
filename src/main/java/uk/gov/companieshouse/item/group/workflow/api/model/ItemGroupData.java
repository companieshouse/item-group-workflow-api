package uk.gov.companieshouse.item.group.workflow.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemGroupData {
    private String companyNumber;
    public String getCompanyNumber() {
        return companyNumber;
    }
    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }
    //
    // company_name
    //
    private String companyName;
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}