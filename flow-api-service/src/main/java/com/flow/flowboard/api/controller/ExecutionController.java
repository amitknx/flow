package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.ExecutionService;
import com.flow.flowboard.common.dto.execution.ExecutionCreateRequest;
import com.flow.flowboard.common.dto.execution.ExecutionResponse;
import com.flow.flowboard.common.dto.execution.ExecutionStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/executions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping
    public ResponseEntity<ExecutionResponse> createExecution(@Valid @RequestBody ExecutionCreateRequest request) {
        ExecutionResponse response = executionService.createExecution(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecutionResponse> getExecutionById(@PathVariable("id") UUID id) {
        ExecutionResponse response = executionService.getExecutionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ExecutionStatusResponse> getExecutionStatus(@PathVariable("id") UUID id) {
        ExecutionStatusResponse response = executionService.getExecutionStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ExecutionResponse>> getAllExecutions(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ExecutionResponse> response = executionService.getAllExecutions(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workflows/{workflowId}")
    public ResponseEntity<Page<ExecutionResponse>> getExecutionsByWorkflow(
        @PathVariable("workflowId") UUID workflowId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ExecutionResponse> response = executionService.listExecutionsByWorkflow(workflowId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ExecutionResponse> cancelExecution(@PathVariable("id") UUID id) {
        ExecutionResponse response = executionService.cancelExecution(id);
        return ResponseEntity.ok(response);
    }
}
