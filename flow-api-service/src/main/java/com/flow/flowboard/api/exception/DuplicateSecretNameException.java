package com.flow.flowboard.api.exception;

/**
 * Exception thrown when attempting to create a secret with a name that already exists
 */
public class DuplicateSecretNameException extends RuntimeException {

    public DuplicateSecretNameException(String name) {
        super(String.format("Secret with name '%s' already exists", name));
    }
}
