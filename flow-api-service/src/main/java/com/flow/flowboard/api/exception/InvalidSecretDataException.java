package com.flow.flowboard.api.exception;

/**
 * Exception thrown when secret data is invalid for the specified auth type
 */
public class InvalidSecretDataException extends RuntimeException {

    public InvalidSecretDataException(String message) {
        super(message);
    }

    public InvalidSecretDataException(String authType, String requiredFields) {
        super(String.format("Invalid secret data for auth type %s. Required fields: %s",
                authType, requiredFields));
    }
}
