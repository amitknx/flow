package com.flow.flowboard.api.service;

import com.flow.flowboard.api.entity.Workflow;
import com.flow.flowboard.api.exception.WorkflowNotFoundException;
import com.flow.flowboard.api.repository.WorkflowRepository;
import com.flow.flowboard.common.dto.workflow.WorkflowCreateRequest;
import com.flow.flowboard.common.dto.workflow.WorkflowDefinitionDto;
import com.flow.flowboard.common.dto.workflow.WorkflowResponse;
import com.flow.flowboard.common.dto.workflow.WorkflowUpdateRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkflowResponse create(WorkflowCreateRequest request) {
        Workflow workflow = Workflow.builder()
            .name(request.getName())
            .description(request.getDescription())
            .data(toJsonString(request.getData()))
            .build();

        Workflow saved = workflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WorkflowResponse getById(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
            .orElseThrow(() -> new WorkflowNotFoundException(id));
        return toResponse(workflow);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowResponse> getAll(Pageable pageable) {
        return workflowRepository.findByActiveTrue(pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowResponse> search(String query, Pageable pageable) {
        return workflowRepository.searchActiveWorkflows(query, pageable)
            .map(this::toResponse);
    }

    @Transactional
    public WorkflowResponse update(UUID id, WorkflowUpdateRequest request) {
        Workflow workflow = workflowRepository.findById(id)
            .orElseThrow(() -> new WorkflowNotFoundException(id));

        if (request.getName() != null) {
            workflow.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workflow.setDescription(request.getDescription());
        }
        if (request.getData() != null) {
            workflow.setData(toJsonString(request.getData()));
        }
        if (request.getActive() != null) {
            workflow.setActive(request.getActive());
        }

        Workflow updated = workflowRepository.save(workflow);
        return toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        Workflow workflow = workflowRepository.findById(id)
            .orElseThrow(() -> new WorkflowNotFoundException(id));
        workflow.setActive(false);
        workflowRepository.save(workflow);
    }

    @Transactional
    public WorkflowResponse clone(UUID id, String newName) {
        Workflow source = workflowRepository.findById(id)
            .orElseThrow(() -> new WorkflowNotFoundException(id));

        Workflow cloned = Workflow.builder()
            .name(newName != null ? newName : source.getName() + " (Copy)")
            .description(source.getDescription())
            .data(source.getData())
            .build();

        Workflow saved = workflowRepository.save(cloned);
        return toResponse(saved);
    }

    private WorkflowResponse toResponse(Workflow workflow) {
        return WorkflowResponse.builder()
            .id(workflow.getId())
            .name(workflow.getName())
            .description(workflow.getDescription())
            .data(parseJsonString(workflow.getData()))
            .active(workflow.getActive())
            .createdAt(workflow.getCreatedAt())
            .createdBy(workflow.getCreatedBy())
            .modifiedAt(workflow.getModifiedAt())
            .modifiedBy(workflow.getModifiedBy())
            .build();
    }

    private String toJsonString(WorkflowDefinitionDto data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize data to JSON", e);
        }
    }

    private WorkflowDefinitionDto parseJsonString(String json) {
        try {
            return objectMapper.readValue(json, WorkflowDefinitionDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON data", e);
        }
    }
}
