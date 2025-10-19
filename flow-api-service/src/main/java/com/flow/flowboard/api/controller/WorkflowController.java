package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.WorkflowService;
import com.flow.flowboard.common.dto.workflow.WorkflowCreateRequest;
import com.flow.flowboard.common.dto.workflow.WorkflowResponse;
import com.flow.flowboard.common.dto.workflow.WorkflowUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<WorkflowResponse> create(@Valid @RequestBody WorkflowCreateRequest request) {
        WorkflowResponse response = workflowService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getById(@PathVariable UUID id) {
        WorkflowResponse response = workflowService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<WorkflowResponse>> getAll(
        @RequestParam(required = false) String search,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<WorkflowResponse> response = search != null && !search.isBlank()
            ? workflowService.search(search, pageable)
            : workflowService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody WorkflowUpdateRequest request
    ) {
        WorkflowResponse response = workflowService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workflowService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<WorkflowResponse> clone(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body
    ) {
        String newName = body != null ? body.get("name") : null;
        WorkflowResponse response = workflowService.clone(id, newName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
