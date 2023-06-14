package uk.gov.companieshouse.itemgroupworkflowapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import static uk.gov.companieshouse.itemgroupworkflowapi.util.Constants.JSON_DATE_TIME_FORMAT;

/**
 * This entity gets persisted to the item_groups collection with @Id and create/update timestamps.<p>
 * It contains the JSON payload (an instance of ItemGroupData) as a data member<p>
 */
@Document(collection = "item_groups")
public class ItemGroup {
    @Id
    private String id;
    public String getId() {
        return id;
    }
    public void setId(final String id){
        this.id = id;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=JSON_DATE_TIME_FORMAT)
    private LocalDateTime createdAt;
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=JSON_DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    private ItemGroupData data  = new ItemGroupData();
    public ItemGroupData getData() {
        return data;
    }
    public void setData(ItemGroupData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}