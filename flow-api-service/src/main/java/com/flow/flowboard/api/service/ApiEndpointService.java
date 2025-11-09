package com.flow.flowboard.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.Api;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.api.exception.ApiEndpointNotFoundException;
import com.flow.flowboard.api.exception.ApiNotFoundException;
import com.flow.flowboard.api.exception.DuplicateApiEndpointNameException;
import com.flow.flowboard.api.repository.ApiEndpointRepository;
import com.flow.flowboard.api.repository.ApiRepository;
import com.flow.flowboard.common.dto.api.ApiEndpointDto;
import com.flow.flowboard.common.dto.api.ApiEndpointResponse;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing API endpoints
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiEndpointService {

    private final ApiEndpointRepository endpointRepository;
    private final ApiRepository apiRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiEndpointResponse createEndpoint(UUID apiId, ApiEndpointDto request) {
        // Validate parent API exists
        Api api = apiRepository.findByIdAndActiveTrue(apiId)
                .orElseThrow(() -> new ApiNotFoundException(apiId));

        // Validate unique name within API
        if (endpointRepository.existsByApiIdAndNameIgnoreCaseAndActiveTrue(apiId, request.getName())) {
            throw new DuplicateApiEndpointNameException(apiId, request.getName());
        }

        // Create endpoint
        ApiEndpoint endpoint = ApiEndpoint.builder()
                .api(api)
                .name(request.getName())
                .description(request.getDescription())
                .httpMethod(request.getHttpMethod())
                .endpointPath(request.getEndpointPath())
                .pathParams(toJsonString(request.getPathParams()))
                .queryParams(toJsonString(request.getQueryParams()))
                .headers(toJsonString(request.getHeaders()))
                .requestBodyTemplate(toJsonString(request.getRequestBodyTemplate()))
                .contentType(request.getContentType())
                .responseMapping(toJsonString(request.getResponseMapping()))
                .timeoutMs(request.getTimeoutMs())
                .retryConfig(toJsonString(request.getRetryConfig()))
                .build();

        ApiEndpoint saved = endpointRepository.save(endpoint);

        log.info("Created endpoint: {} for API: {} {}",
                saved.getName(), api.getName(), api.getVersion());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApiEndpointResponse getEndpointById(UUID id) {
        ApiEndpoint endpoint = endpointRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiEndpointNotFoundException(id));

        return toResponse(endpoint);
    }

    @Transactional(readOnly = true)
    public ApiEndpointResponse getEndpointByName(UUID apiId, String name) {
        ApiEndpoint endpoint = endpointRepository.findByApi_IdAndNameAndActiveTrue(apiId, name)
                .orElseThrow(() -> new ApiEndpointNotFoundException(apiId, name));

        return toResponse(endpoint);
    }

    @Transactional(readOnly = true)
    public List<ApiEndpointResponse> getAllEndpointsByApi(UUID apiId) {
        return endpointRepository.findByApi_IdAndActiveTrueOrderByCreatedAt(apiId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ApiEndpointResponse> getAllEndpoints(Pageable pageable) {
        return endpointRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApiEndpointResponse> searchEndpoints(String searchTerm, Pageable pageable) {
        return endpointRepository.searchActiveEndpoints(searchTerm, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApiEndpointResponse> searchEndpointsByApi(UUID apiId, String searchTerm, Pageable pageable) {
        return endpointRepository.searchActiveEndpointsByApi(apiId, searchTerm, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ApiEndpointResponse updateEndpoint(UUID id, ApiEndpointDto request) {
        ApiEndpoint endpoint = endpointRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiEndpointNotFoundException(id));

        // Update fields selectively
        if (request.getName() != null && !request.getName().equals(endpoint.getName())) {
            // Check if new name is unique within the API
            if (endpointRepository.existsByApiIdAndNameIgnoreCaseAndActiveTrue(
                    endpoint.getApi().getId(), request.getName())) {
                throw new DuplicateApiEndpointNameException(endpoint.getApi().getId(), request.getName());
            }
            endpoint.setName(request.getName());
        }
        if (request.getDescription() != null) {
            endpoint.setDescription(request.getDescription());
        }
        if (request.getHttpMethod() != null) {
            endpoint.setHttpMethod(request.getHttpMethod());
        }
        if (request.getEndpointPath() != null) {
            endpoint.setEndpointPath(request.getEndpointPath());
        }
        if (request.getPathParams() != null) {
            endpoint.setPathParams(toJsonString(request.getPathParams()));
        }
        if (request.getQueryParams() != null) {
            endpoint.setQueryParams(toJsonString(request.getQueryParams()));
        }
        if (request.getHeaders() != null) {
            endpoint.setHeaders(toJsonString(request.getHeaders()));
        }
        if (request.getRequestBodyTemplate() != null) {
            endpoint.setRequestBodyTemplate(toJsonString(request.getRequestBodyTemplate()));
        }
        if (request.getContentType() != null) {
            endpoint.setContentType(request.getContentType());
        }
        if (request.getResponseMapping() != null) {
            endpoint.setResponseMapping(toJsonString(request.getResponseMapping()));
        }
        if (request.getTimeoutMs() != null) {
            endpoint.setTimeoutMs(request.getTimeoutMs());
        }
        if (request.getRetryConfig() != null) {
            endpoint.setRetryConfig(toJsonString(request.getRetryConfig()));
        }

        ApiEndpoint updated = endpointRepository.save(endpoint);

        log.info("Updated endpoint: {}", updated.getName());

        return toResponse(updated);
    }

    @Transactional
    public void deleteEndpoint(UUID id) {
        ApiEndpoint endpoint = endpointRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiEndpointNotFoundException(id));

        // Soft delete
        endpoint.setActive(false);
        endpointRepository.save(endpoint);

        log.info("Deleted endpoint: {}", endpoint.getName());
    }

    /**
     * Get endpoint for workflow execution (internal use)
     */
    @Transactional(readOnly = true)
    public ApiEndpoint getEndpointForExecution(UUID apiId, String endpointName) {
        return endpointRepository.findByApi_IdAndNameAndActiveTrue(apiId, endpointName)
                .orElseThrow(() -> new ApiEndpointNotFoundException(apiId, endpointName));
    }

    /**
     * Convert entity to response DTO
     */
    private ApiEndpointResponse toResponse(ApiEndpoint endpoint) {
        Api api = endpoint.getApi();

        return ApiEndpointResponse.builder()
                .id(endpoint.getId())
                .apiId(api.getId())
                .apiName(api.getName())
                .apiVersion(api.getVersion())
                .name(endpoint.getName())
                .description(endpoint.getDescription())
                .httpMethod(endpoint.getHttpMethod())
                .endpointPath(endpoint.getEndpointPath())
                .pathParams(parseJsonStringToMap(endpoint.getPathParams()))
                .queryParams(parseJsonStringToMap(endpoint.getQueryParams()))
                .headers(parseJsonStringToMapString(endpoint.getHeaders()))
                .requestBodyTemplate(parseJsonStringToObject(endpoint.getRequestBodyTemplate()))
                .contentType(endpoint.getContentType())
                .responseMapping(parseJsonStringToMapString(endpoint.getResponseMapping()))
                .timeoutMs(endpoint.getTimeoutMs())
                .retryConfig(parseJsonStringToRetryConfig(endpoint.getRetryConfig()))
                .active(endpoint.getActive())
                .createdAt(endpoint.getCreatedAt())
                .createdBy(endpoint.getCreatedBy())
                .modifiedAt(endpoint.getModifiedAt())
                .modifiedBy(endpoint.getModifiedBy())
                .build();
    }

    private String toJsonString(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize data to JSON", e);
            throw new RuntimeException("Failed to serialize data", e);
        }
    }

    private Map<String, Object> parseJsonStringToMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to map", e);
            return null;
        }
    }

    private Map<String, String> parseJsonStringToMapString(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to map", e);
            return null;
        }
    }

    private Object parseJsonStringToObject(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to object", e);
            return null;
        }
    }

    private RetryConfigDto parseJsonStringToRetryConfig(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, RetryConfigDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to RetryConfigDto", e);
            return null;
        }
    }
}
