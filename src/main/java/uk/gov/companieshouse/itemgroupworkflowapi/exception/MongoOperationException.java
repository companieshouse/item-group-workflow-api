package uk.gov.companieshouse.itemgroupworkflowapi.exception;

public class MongoOperationException extends RuntimeException {
    public MongoOperationException(String message, Throwable cause) { super(message, cause); }
}
