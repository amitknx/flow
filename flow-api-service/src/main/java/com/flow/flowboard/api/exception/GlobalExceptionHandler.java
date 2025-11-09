package com.flow.flowboard.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WorkflowNotFoundException.class)
    public ProblemDetail handleWorkflowNotFound(WorkflowNotFoundException ex) {
        log.warn("Workflow not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/workflow-not-found"));
        problemDetail.setTitle("Workflow Not Found");
        return problemDetail;
    }

    @ExceptionHandler(ExecutionNotFoundException.class)
    public ProblemDetail handleExecutionNotFound(ExecutionNotFoundException ex) {
        log.warn("Execution not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/execution-not-found"));
        problemDetail.setTitle("Execution Not Found");
        return problemDetail;
    }

    @ExceptionHandler(InvalidExecutionStateException.class)
    public ProblemDetail handleInvalidExecutionState(InvalidExecutionStateException ex) {
        log.warn("Invalid execution state: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/invalid-execution-state"));
        problemDetail.setTitle("Invalid Execution State");
        return problemDetail;
    }

    @ExceptionHandler(SecretNotFoundException.class)
    public ProblemDetail handleSecretNotFound(SecretNotFoundException ex) {
        log.warn("Secret not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/secret-not-found"));
        problemDetail.setTitle("Secret Not Found");
        return problemDetail;
    }

    @ExceptionHandler(DuplicateSecretNameException.class)
    public ProblemDetail handleDuplicateSecretName(DuplicateSecretNameException ex) {
        log.warn("Duplicate secret name: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/duplicate-secret-name"));
        problemDetail.setTitle("Duplicate Secret Name");
        return problemDetail;
    }

    @ExceptionHandler(InvalidSecretDataException.class)
    public ProblemDetail handleInvalidSecretData(InvalidSecretDataException ex) {
        log.warn("Invalid secret data: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/invalid-secret-data"));
        problemDetail.setTitle("Invalid Secret Data");
        return problemDetail;
    }

    @ExceptionHandler(ApiDefinitionNotFoundException.class)
    public ProblemDetail handleApiDefinitionNotFound(ApiDefinitionNotFoundException ex) {
        log.warn("API definition not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/api-definition-not-found"));
        problemDetail.setTitle("API Definition Not Found");
        return problemDetail;
    }

    @ExceptionHandler(ApiNotFoundException.class)
    public ProblemDetail handleApiNotFound(ApiNotFoundException ex) {
        log.warn("API not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/api-not-found"));
        problemDetail.setTitle("API Not Found");
        return problemDetail;
    }

    @ExceptionHandler(DuplicateApiException.class)
    public ProblemDetail handleDuplicateApi(DuplicateApiException ex) {
        log.warn("Duplicate API: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/duplicate-api"));
        problemDetail.setTitle("Duplicate API");
        return problemDetail;
    }

    @ExceptionHandler(ApiEndpointNotFoundException.class)
    public ProblemDetail handleApiEndpointNotFound(ApiEndpointNotFoundException ex) {
        log.warn("API endpoint not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/api-endpoint-not-found"));
        problemDetail.setTitle("API Endpoint Not Found");
        return problemDetail;
    }

    @ExceptionHandler(DuplicateApiEndpointNameException.class)
    public ProblemDetail handleDuplicateApiEndpointName(DuplicateApiEndpointNameException ex) {
        log.warn("Duplicate API endpoint name: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/duplicate-api-endpoint-name"));
        problemDetail.setTitle("Duplicate API Endpoint Name");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.debug("Validation error", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Validation failed"
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/validation-failed"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
        );
        problemDetail.setType(URI.create("https://flowboard.flow.com/errors/internal-error"));
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }
}
