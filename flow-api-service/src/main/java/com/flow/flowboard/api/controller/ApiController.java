package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.ApiService;
import com.flow.flowboard.common.dto.api.ApiCreateRequest;
import com.flow.flowboard.common.dto.api.ApiResponse;
import com.flow.flowboard.common.dto.api.ApiUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for API management (top-level API configurations)
 *
 * Endpoints:
 * - POST   /api/v1/apis                     - Create new API (optionally with endpoints)
 * - GET    /api/v1/apis                     - List all APIs (paginated)
 * - GET    /api/v1/apis/{id}                - Get API by ID
 * - GET    /api/v1/apis/{name}/{version}    - Get API by name and version
 * - GET    /api/v1/apis/credential/{credId} - Get APIs by credential
 * - PUT    /api/v1/apis/{id}                - Update API
 * - DELETE /api/v1/apis/{id}                - Delete API (soft delete, cascades to endpoints)
 */
@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ApiController {

    private final ApiService apiService;

    /**
     * Create a new API (optionally with endpoints)
     *
     * @param request API creation request (can include endpoints)
     * @return created API
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createApi(@Valid @RequestBody ApiCreateRequest request) {
        ApiResponse response = apiService.createApi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get API by ID
     *
     * @param id API ID
     * @return API details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getApiById(@PathVariable("id") UUID id) {
        ApiResponse response = apiService.getApiById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API by name and version
     *
     * @param name API name
     * @param version API version
     * @return API details
     */
    @GetMapping("/{name}/{version}")
    public ResponseEntity<ApiResponse> getApiByNameAndVersion(
            @PathVariable("name") String name,
            @PathVariable("version") String version) {
        ApiResponse response = apiService.getApiByNameAndVersion(name, version);
        return ResponseEntity.ok(response);
    }

    /**
     * List all APIs with optional search
     *
     * @param search optional search term for name/description/version
     * @param pageable pagination parameters
     * @return page of APIs
     */
    @GetMapping
    public ResponseEntity<Page<ApiResponse>> getAllApis(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ApiResponse> response;
        if (search != null && !search.isBlank()) {
            response = apiService.searchApis(search, pageable);
        } else {
            response = apiService.getAllApis(pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing API
     *
     * @param id API ID
     * @param request update request with fields to change
     * @return updated API
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateApi(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ApiUpdateRequest request) {
        ApiResponse response = apiService.updateApi(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an API (soft delete, cascades to all endpoints)
     *
     * @param id API ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable("id") UUID id) {
        apiService.deleteApi(id);
        return ResponseEntity.noContent().build();
    }
}
