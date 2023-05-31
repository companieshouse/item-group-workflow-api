package uk.gov.companieshouse.item.group.workflow.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
/**
 * This entity gets persisted to the item_group collection with @Id and create/update timestamps.<p>
 * It contains the JSON payload (an instance of ItemGroupJsonPayload) as a data member<p>
 * AFAIK Spring currently has no way of parameterizing the collection name, so it gets hard-coded for now.
 */
@Document(collection = "item_group")
public class ItemGroupCreate {
    //
    // Entity ID.
    //
    @Id
    private String id;
    public final String getId() {
        return id;
    }
    public void setId(final String id){
        this.id = id;
    }
    //
    // Create and Update timestamps.
    //
    private LocalDateTime createdAt;
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private LocalDateTime updatedAt;
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    //
    // Contained JSON payload.
    //
    private ItemGroupJsonPayload data  = new ItemGroupJsonPayload();
    public final ItemGroupJsonPayload getData() {
        return data;
    }
    public void setData(ItemGroupJsonPayload data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ItemGroupCreate {" +
            data +
            '}';
    }
}