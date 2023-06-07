package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import org.springframework.data.mongodb.core.mapping.Field;

public class DeliveryDetails {

    @JsonProperty("address_line_1")
    @Field("address_line_1")
    private String addressLine1;

    @JsonProperty("address_line_2")
    @Field("address_line_2")
    private String addressLine2;

    @JsonProperty("company_name")
    @Field("company_name")
    private String companyName;

    @JsonProperty("country")
    private String country;

    @JsonProperty("forename")
    private String forename;

    @JsonProperty("locality")
    private String locality;

    @JsonProperty("po_box")
    @Field("po_box")
    private String poBox;

    @JsonProperty("postal_code")
    @Field("postal_code")
    private String postalCode;

    @JsonProperty("region")
    private String region;

    @JsonProperty("surname")
    private String surname;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
