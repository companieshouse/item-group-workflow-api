package uk.gov.companieshouse.item.group.workflow.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "item_group")
public class ItemGroup {
    //
    // Entity ID.
    //
    @Id
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
//        data.setId(id);
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
    // Contained TestDtoData
    //
    private ItemGroupData data  = new ItemGroupData();
    public final ItemGroupData getData() {
        return data;
    }
    public void setData(ItemGroupData data) {
        this.data = data;
    }
    //
    // Forwarded to TestDtoData
    //
//    public final String getCompanyName() {
//        return data.getCompanyName();
//    }
//    public void setCompanyName(String companyName) {
//        data.setCompanyName(companyName);
//    }
//
//    public final String getCompanyNumber() {
//        return data.getCompanyNumber();
//    }
//    public void setCompanyNumber(String companyNumber) {
//        data.setCompanyNumber(companyNumber);
//    }
//
//    @Override
//    public String toString() {
//        return "TestDTO {" +
//            "companyName='" + data.getCompanyNumber() + '\'' +
//            ", companyNumber='" + data.getCompanyName() + '}';
//    }
}
