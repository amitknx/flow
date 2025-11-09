package com.flow.flowboard.api.exception;

import java.util.UUID;

/**
 * Exception thrown when an API is not found
 */
public class ApiNotFoundException extends RuntimeException {

    public ApiNotFoundException(UUID id) {
        super(String.format("API not found with id: %s", id));
    }

    public ApiNotFoundException(String name, String version) {
        super(String.format("API not found with name: %s and version: %s", name, version));
    }
}
