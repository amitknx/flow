package com.flow.flowboard.api.exception;

/**
 * Exception thrown when attempting to create an API with a name+version that already exists
 */
public class DuplicateApiException extends RuntimeException {

    public DuplicateApiException(String name, String version) {
        super(String.format("API with name '%s' and version '%s' already exists", name, version));
    }
}
