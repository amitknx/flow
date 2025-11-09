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

        // Update endpoints if provided - replace all existing endpoints
        if (request.getEndpoints() != null) {
            // Remove all existing endpoints
            api.getEndpoints().clear();

            // Add new endpoints
            for (ApiEndpointDto endpointDto : request.getEndpoints()) {
                ApiEndpoint endpoint = createEndpointEntity(api, endpointDto);
                api.addEndpoint(endpoint);
            }
        }

        if (request.getActive() != null) {
            api.setActive(request.getActive());
        }

        Api updated = apiRepository.save(api);

        log.info("Updated API: {} {} with {} endpoints",
                updated.getName(), updated.getVersion(), updated.getEndpoints().size());

        return toResponse(updated);
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
        // Count endpoints
        int endpointCount = (int) endpointRepository.countByApi_IdAndActiveTrue(api.getId());

        return ApiResponse.builder()
                .id(api.getId())
                .name(api.getName())
                .version(api.getVersion())
                .baseUrl(api.getBaseUrl())
                .description(api.getDescription())
                .tags(api.getTags())
                .timeoutMs(api.getTimeoutMs())
                .retryConfig(parseJsonStringToRetryConfig(api.getRetryConfig()))
                .endpointCount(endpointCount)
                .active(api.getActive())
                .createdAt(api.getCreatedAt())
                .createdBy(api.getCreatedBy())
                .modifiedAt(api.getModifiedAt())
                .modifiedBy(api.getModifiedBy())
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
}
