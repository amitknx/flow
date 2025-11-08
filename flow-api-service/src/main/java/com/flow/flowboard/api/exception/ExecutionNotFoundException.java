package com.flow.flowboard.api.exception;

import java.util.UUID;

public class ExecutionNotFoundException extends RuntimeException {

    public ExecutionNotFoundException(UUID id) {
        super("Execution not found with id: " + id);
    }
}
