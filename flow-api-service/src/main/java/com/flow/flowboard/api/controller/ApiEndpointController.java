package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.ApiEndpointService;
import com.flow.flowboard.api.service.ApiExecutorService;
import com.flow.flowboard.common.dto.api.ApiEndpointDto;
import com.flow.flowboard.common.dto.api.ApiEndpointResponse;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointRequest;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for API endpoint management and execution
 *
 * Endpoints can be managed either:
 * 1. During API creation (nested in ApiCreateRequest)
 * 2. Individually via these dedicated endpoints
 *
 * REST Endpoints:
 * - POST   /api/v1/apis/{apiId}/endpoints           - Create endpoint in an API
 * - GET    /api/v1/apis/{apiId}/endpoints           - List all endpoints in an API
 * - GET    /api/v1/endpoints/{id}                   - Get endpoint by ID
 * - GET    /api/v1/apis/{apiId}/endpoints/{name}    - Get endpoint by name within API
 * - PUT    /api/v1/endpoints/{id}                   - Update endpoint
 * - DELETE /api/v1/endpoints/{id}                   - Delete endpoint (soft delete)
 * - POST   /api/v1/endpoints/{id}/execute           - Execute endpoint with parameters
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApiEndpointController {

    private final ApiEndpointService endpointService;
    private final ApiExecutorService executorService;

    /**
     * Create a new endpoint in an API
     *
     * @param apiId API ID
     * @param request endpoint creation request
     * @return created endpoint
     */
    @PostMapping("/api/v1/apis/{apiId}/endpoints")
    public ResponseEntity<ApiEndpointResponse> createEndpoint(
            @PathVariable("apiId") UUID apiId,
            @Valid @RequestBody ApiEndpointDto request) {
        ApiEndpointResponse response = endpointService.createEndpoint(apiId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get endpoint by ID
     *
     * @param id endpoint ID
     * @return endpoint details
     */
    @GetMapping("/api/v1/endpoints/{id}")
    public ResponseEntity<ApiEndpointResponse> getEndpointById(@PathVariable("id") UUID id) {
        ApiEndpointResponse response = endpointService.getEndpointById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get endpoint by name within an API
     *
     * @param apiId API ID
     * @param name endpoint name
     * @return endpoint details
     */
    @GetMapping("/api/v1/apis/{apiId}/endpoints/{name}")
    public ResponseEntity<ApiEndpointResponse> getEndpointByName(
            @PathVariable("apiId") UUID apiId,
            @PathVariable("name") String name) {
        ApiEndpointResponse response = endpointService.getEndpointByName(apiId, name);
        return ResponseEntity.ok(response);
    }

    /**
     * List all endpoints in an API (without pagination)
     *
     * @param apiId API ID
     * @return list of endpoints
     */
    @GetMapping("/api/v1/apis/{apiId}/endpoints")
    public ResponseEntity<List<ApiEndpointResponse>> getAllEndpointsByApi(
            @PathVariable("apiId") UUID apiId,
            @RequestParam(required = false) String search) {

        // If search is provided, use paginated search; otherwise return all
        if (search != null && !search.isBlank()) {
            Page<ApiEndpointResponse> page = endpointService.searchEndpointsByApi(
                    apiId, search, Pageable.unpaged());
            return ResponseEntity.ok(page.getContent());
        }

        List<ApiEndpointResponse> response = endpointService.getAllEndpointsByApi(apiId);
        return ResponseEntity.ok(response);
    }

    /**
     * Search all endpoints across all APIs
     *
     * @param search search term
     * @param pageable pagination parameters
     * @return page of endpoints
     */
    @GetMapping("/api/v1/endpoints")
    public ResponseEntity<Page<ApiEndpointResponse>> searchAllEndpoints(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ApiEndpointResponse> response;
        if (search != null && !search.isBlank()) {
            response = endpointService.searchEndpoints(search, pageable);
        } else {
            response = endpointService.getAllEndpoints(pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing endpoint
     *
     * @param id endpoint ID
     * @param request update request with fields to change
     * @return updated endpoint
     */
    @PutMapping("/api/v1/endpoints/{id}")
    public ResponseEntity<ApiEndpointResponse> updateEndpoint(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ApiEndpointDto request) {
        ApiEndpointResponse response = endpointService.updateEndpoint(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an endpoint (soft delete)
     *
     * @param id endpoint ID
     * @return no content
     */
    @DeleteMapping("/api/v1/endpoints/{id}")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable("id") UUID id) {
        endpointService.deleteEndpoint(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Execute an API endpoint
     * This endpoint allows testing and executing configured API endpoints with:
     * - Path parameters (e.g., {{userId}})
     * - Query parameters
     * - Custom headers
     * - Request body (for POST/PUT/PATCH)
     * - Override authentication
     * - Custom timeout
     * Returns detailed execution results including:
     * - HTTP status and response
     * - Response headers
     * - Response body (parsed JSON or raw string)
     * - Execution timing
     * - Error details (if any)
     *
     * @param id endpoint ID to execute
     * @param request execution parameters
     * @return execution response wrapped in Mono (reactive)
     */
    @PostMapping("/api/v1/endpoints/{id}/execute")
    public Mono<ResponseEntity<ExecuteEndpointResponse>> executeEndpoint(
            @PathVariable("id") UUID id,
            @Valid @RequestBody(required = false) ExecuteEndpointRequest request) {

        // Use empty request if not provided
        ExecuteEndpointRequest executionRequest = request != null
                ? request
                : ExecuteEndpointRequest.builder().build();

        return executorService.executeEndpoint(id, executionRequest)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
