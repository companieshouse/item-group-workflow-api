package uk.gov.companieshouse.itemgroupworkflowapi.model;

import java.time.LocalDateTime;

/**
 * Represents objects bearing created at and updated at timestamp properties.
 */
public interface TimestampedEntity {

   LocalDateTime getCreatedAt();

   LocalDateTime getUpdatedAt();

}
