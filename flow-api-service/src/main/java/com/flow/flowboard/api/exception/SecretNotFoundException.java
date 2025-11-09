package com.flow.flowboard.api.exception;

import java.util.UUID;

/**
 * Exception thrown when a secret is not found
 */
public class SecretNotFoundException extends RuntimeException {

    public SecretNotFoundException(UUID id) {
        super(String.format("Secret not found with id: %s", id));
    }

    public SecretNotFoundException(String name) {
        super(String.format("Secret not found with name: %s", name));
    }
}
