package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.google.gson.Gson;
import org.springframework.data.mongodb.core.mapping.Field;

public class OrderedBy {

    private String email;

    @Field("id")
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
        return new Gson().toJson(this);
    }
}
