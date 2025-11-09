package com.flow.flowboard.api.exception;

import java.util.UUID;

/**
 * Exception thrown when an API endpoint is not found
 */
public class ApiEndpointNotFoundException extends RuntimeException {

    public ApiEndpointNotFoundException(UUID id) {
        super(String.format("API endpoint not found with id: %s", id));
    }

    public ApiEndpointNotFoundException(UUID apiId, String name) {
        super(String.format("API endpoint not found with name '%s' in API id: %s", name, apiId));
    }
}
