package uk.gov.companieshouse.item.group.workflow.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderedBy {

    @JsonProperty("email")
    private String email;

    @JsonProperty("id")
    private String id;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ordered_by {" +
                "email='" + email + '\'' +
                ", id='" + id +
                '}';
    }
}
