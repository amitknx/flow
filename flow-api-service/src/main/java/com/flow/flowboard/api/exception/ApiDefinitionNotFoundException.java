package com.flow.flowboard.api.exception;

import java.util.UUID;

/**
 * Exception thrown when an API definition is not found
 */
public class ApiDefinitionNotFoundException extends RuntimeException {

    public ApiDefinitionNotFoundException(UUID id) {
        super(String.format("API definition not found with id: %s", id));
    }

    public ApiDefinitionNotFoundException(UUID apiId, String name) {
        super(String.format("API definition not found with name '%s' in API id: %s", name, apiId));
    }
}
