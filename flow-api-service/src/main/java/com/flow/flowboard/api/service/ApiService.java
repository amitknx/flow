package com.flow.flowboard.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.Api;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.api.exception.ApiNotFoundException;
import com.flow.flowboard.api.exception.DuplicateApiException;
import com.flow.flowboard.api.repository.ApiEndpointRepository;
import com.flow.flowboard.api.repository.ApiRepository;
import com.flow.flowboard.common.dto.api.ApiCreateRequest;
import com.flow.flowboard.common.dto.api.ApiEndpointDto;
import com.flow.flowboard.common.dto.api.ApiResponse;
import com.flow.flowboard.common.dto.api.ApiUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing APIs (top-level API configurations)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    private final ApiRepository apiRepository;
    private final ApiEndpointRepository endpointRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ApiResponse createApi(ApiCreateRequest request) {
        // Validate unique name + version
        if (apiRepository.existsByNameAndVersionIgnoreCaseAndActiveTrue(
                request.getName(), request.getVersion())) {
            throw new DuplicateApiException(request.getName(), request.getVersion());
        }

        // Create API entity
        Api api = Api.builder()
                .name(request.getName())
                .version(request.getVersion())
                .baseUrl(request.getBaseUrl())
                .description(request.getDescription())
                .authentication(toJsonString(request.getAuthentication()))
                .tags(request.getTags())
                .timeoutMs(request.getTimeoutMs())
                .retryConfig(toJsonString(request.getRetryConfig()))
                .build();

        // Create endpoints if provided
        if (request.getEndpoints() != null && !request.getEndpoints().isEmpty()) {
            for (ApiEndpointDto endpointDto : request.getEndpoints()) {
                ApiEndpoint endpoint = createEndpointEntity(api, endpointDto);
                api.addEndpoint(endpoint);
            }
        }

        Api saved = apiRepository.save(api);

        log.info("Created API: {} {} with {} endpoints",
                saved.getName(), saved.getVersion(), saved.getEndpoints().size());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse getApiById(UUID id) {
        Api api = apiRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiNotFoundException(id));

        return toResponse(api);
    }

    @Transactional(readOnly = true)
    public ApiResponse getApiByNameAndVersion(String name, String version) {
        Api api = apiRepository.findByNameAndVersionAndActiveTrue(name, version)
                .orElseThrow(() -> new ApiNotFoundException(name, version));

        return toResponse(api);
    }

    @Transactional(readOnly = true)
    public Page<ApiResponse> getAllApis(Pageable pageable) {
        return apiRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApiResponse> searchApis(String searchTerm, Pageable pageable) {
        return apiRepository.searchActiveApis(searchTerm, pageable)
                .map(this::toResponse);
    }


    @Transactional
    public ApiResponse updateApi(UUID id, ApiUpdateRequest request) {
        Api api = apiRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiNotFoundException(id));

        // Validate version number for optimistic locking
        if (!api.getVersionNum().equals(request.getVersionNum())) {
            throw new IllegalStateException(
                    "API has been modified by another user. Please refresh and try again. " +
                    "Expected version: " + request.getVersionNum() + ", Current version: " + api.getVersionNum());
        }

        // Update fields selectively
        String newName = request.getName() != null ? request.getName() : api.getName();
        String newVersion = request.getVersion() != null ? request.getVersion() : api.getVersion();

        // Check if name+version combination has changed and validate uniqueness
        if (!newName.equals(api.getName()) || !newVersion.equals(api.getVersion())) {
            if (apiRepository.existsByNameAndVersionIgnoreCaseAndActiveTrue(newName, newVersion)) {
                throw new DuplicateApiException(newName, newVersion);
            }
        }

        if (request.getName() != null) {
            api.setName(request.getName());
        }
        if (request.getVersion() != null) {
            api.setVersion(request.getVersion());
        }
        if (request.getBaseUrl() != null) {
            api.setBaseUrl(request.getBaseUrl());
        }
        if (request.getDescription() != null) {
            api.setDescription(request.getDescription());
        }
        if (request.getAuthentication() != null) {
            api.setAuthentication(toJsonString(request.getAuthentication()));
        }
        if (request.getTags() != null) {
            api.setTags(request.getTags());
        }
        if (request.getTimeoutMs() != null) {
            api.setTimeoutMs(request.getTimeoutMs());
        }
        if (request.getRetryConfig() != null) {
            api.setRetryConfig(toJsonString(request.getRetryConfig()));
        }

        // Update endpoints if provided - smart update with IDs
        if (request.getEndpoints() != null) {
            updateEndpoints(api, request.getEndpoints());
        }

        if (request.getActive() != null) {
            api.setActive(request.getActive());
        }

        Api updated = apiRepository.save(api);

        log.info("Updated API: {} {} (version {}) with {} endpoints",
                updated.getName(), updated.getVersion(), updated.getVersionNum(), updated.getEndpoints().size());

        return toResponse(updated);
    }

    /**
     * Smart endpoint update logic:
     * - If endpoint has ID and exists: update it (with version check)
     * - If endpoint has no ID: create new endpoint
     * - Endpoints not in the request: remove them
     */
    private void updateEndpoints(Api api, List<ApiEndpointDto> endpointDtos) {

        // Get requested endpoint IDs (non-null)
        List<UUID> requestedEndpointIds = endpointDtos.stream()
                .map(ApiEndpointDto::getId)
                .filter(Objects::nonNull)
                .toList();

        // Remove endpoints not in the request
        api.getEndpoints().removeIf(endpoint ->
                !requestedEndpointIds.contains(endpoint.getId()));

        // Update existing or create new endpoints
        for (ApiEndpointDto dto : endpointDtos) {
            if (dto.getId() != null) {
                // Update existing endpoint
                ApiEndpoint existing = api.getEndpoints().stream()
                        .filter(e -> e.getId().equals(dto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Endpoint with ID " + dto.getId() + " not found in this API"));

                // Check version for optimistic locking
                if (dto.getVersionNum() != null && !existing.getVersionNum().equals(dto.getVersionNum())) {
                    throw new IllegalStateException(
                            "Endpoint '" + dto.getName() + "' has been modified by another user. " +
                            "Expected version: " + dto.getVersionNum() + ", Current version: " + existing.getVersionNum());
                }

                // Update endpoint fields
                updateEndpointFields(existing, dto);
            } else {
                // Create new endpoint
                ApiEndpoint newEndpoint = createEndpointEntity(api, dto);
                api.addEndpoint(newEndpoint);
            }
        }
    }

    /**
     * Update existing endpoint fields from DTO
     */
    private void updateEndpointFields(ApiEndpoint endpoint, ApiEndpointDto dto) {
        endpoint.setName(dto.getName());
        endpoint.setDescription(dto.getDescription());
        endpoint.setHttpMethod(dto.getHttpMethod());
        endpoint.setEndpointPath(dto.getEndpointPath());
        endpoint.setPathParams(toJsonString(dto.getPathParams()));
        endpoint.setQueryParams(toJsonString(dto.getQueryParams()));
        endpoint.setHeaders(toJsonString(dto.getHeaders()));
        endpoint.setRequestBodyTemplate(toJsonString(dto.getRequestBodyTemplate()));
        endpoint.setContentType(dto.getContentType());
        endpoint.setResponseMapping(toJsonString(dto.getResponseMapping()));
        endpoint.setTimeoutMs(dto.getTimeoutMs());
        endpoint.setRetryConfig(toJsonString(dto.getRetryConfig()));
    }

    @Transactional
    public void deleteApi(UUID id) {
        Api api = apiRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ApiNotFoundException(id));

        // Soft delete (cascade will soft delete all endpoints via setter)
        api.setActive(false);
        api.getEndpoints().forEach(endpoint -> endpoint.setActive(false));
        apiRepository.save(api);

        log.info("Deleted API: {} {} and {} endpoints",
                api.getName(), api.getVersion(), api.getEndpoints().size());
    }

    /**
     * Helper method to create ApiEndpoint entity from DTO
     */
    private ApiEndpoint createEndpointEntity(Api api, ApiEndpointDto dto) {
        return ApiEndpoint.builder()
                .api(api)
                .name(dto.getName())
                .description(dto.getDescription())
                .httpMethod(dto.getHttpMethod())
                .endpointPath(dto.getEndpointPath())
                .pathParams(toJsonString(dto.getPathParams()))
                .queryParams(toJsonString(dto.getQueryParams()))
                .headers(toJsonString(dto.getHeaders()))
                .requestBodyTemplate(toJsonString(dto.getRequestBodyTemplate()))
                .contentType(dto.getContentType())
                .responseMapping(toJsonString(dto.getResponseMapping()))
                .timeoutMs(dto.getTimeoutMs())
                .retryConfig(toJsonString(dto.getRetryConfig()))
                .build();
    }

    /**
     * Convert entity to response DTO
     */
    private ApiResponse toResponse(Api api) {
        // Convert endpoints to DTOs
        List<ApiEndpointDto> endpointDtos = api.getEndpoints().stream()
                .filter(ApiEndpoint::getActive)
                .map(this::toEndpointDto)
                .toList();

        return ApiResponse.builder()
                .id(api.getId())
                .versionNum(api.getVersionNum())
                .name(api.getName())
                .version(api.getVersion())
                .baseUrl(api.getBaseUrl())
                .description(api.getDescription())
                .authentication(parseJsonStringToAuthentication(api.getAuthentication()))
                .tags(api.getTags())
                .timeoutMs(api.getTimeoutMs())
                .retryConfig(parseJsonStringToRetryConfig(api.getRetryConfig()))
                .endpoints(endpointDtos)
                .endpointCount(endpointDtos.size())
                .active(api.getActive())
                .createdAt(api.getCreatedAt())
                .createdBy(api.getCreatedBy())
                .modifiedAt(api.getModifiedAt())
                .modifiedBy(api.getModifiedBy())
                .build();
    }

    /**
     * Convert ApiEndpoint entity to DTO
     */
    private ApiEndpointDto toEndpointDto(ApiEndpoint endpoint) {
        return ApiEndpointDto.builder()
                .id(endpoint.getId())
                .versionNum(endpoint.getVersionNum())
                .name(endpoint.getName())
                .description(endpoint.getDescription())
                .httpMethod(endpoint.getHttpMethod())
                .endpointPath(endpoint.getEndpointPath())
                .pathParams(parseJsonStringToMap(endpoint.getPathParams()))
                .queryParams(parseJsonStringToMap(endpoint.getQueryParams()))
                .headers(parseJsonStringToStringMap(endpoint.getHeaders()))
                .requestBodyTemplate(parseJsonStringToObject(endpoint.getRequestBodyTemplate()))
                .contentType(endpoint.getContentType())
                .responseMapping(parseJsonStringToStringMap(endpoint.getResponseMapping()))
                .timeoutMs(endpoint.getTimeoutMs())
                .retryConfig(parseJsonStringToRetryConfig(endpoint.getRetryConfig()))
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

    private List<String> parseJsonStringToList(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to list", e);
            return null;
        }
    }

    private com.flow.flowboard.common.dto.credential.RetryConfigDto parseJsonStringToRetryConfig(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, com.flow.flowboard.common.dto.credential.RetryConfigDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to RetryConfigDto", e);
            return null;
        }
    }

    private com.flow.flowboard.common.dto.auth.ApiAuthenticationDto parseJsonStringToAuthentication(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, com.flow.flowboard.common.dto.auth.ApiAuthenticationDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to ApiAuthenticationDto", e);
            return null;
        }
    }

    private Map<String, Object> parseJsonStringToMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to Map", e);
            return null;
        }
    }

    private Map<String, String> parseJsonStringToStringMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON to String Map", e);
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
            log.error("Failed to parse JSON to Object", e);
            return null;
        }
    }
}
