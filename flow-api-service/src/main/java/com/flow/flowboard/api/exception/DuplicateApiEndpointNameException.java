package com.flow.flowboard.api.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to create an endpoint with a name that already exists in the API
 */
public class DuplicateApiEndpointNameException extends RuntimeException {

    public DuplicateApiEndpointNameException(UUID apiId, String name) {
        super(String.format("API endpoint with name '%s' already exists in API id: %s", name, apiId));
    }
}
