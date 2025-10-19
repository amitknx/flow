/*
 ************************************************************************
 * flow CONFIDENTIAL ___________________
 * Copyright 2024 flow Systems Incorporated All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property of flow Systems
 * Incorporated and its suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary to flow Systems Incorporated and its suppliers and are protected by all
 * applicable intellectual property laws, including trade secret and copyright laws. Dissemination
 * of this information or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from flow Systems Incorporated.
 *************************************************************************
 */
package com.flow.flowboard.authoring.service;

import com.flow.flowboard.authoring.domain.Workflow;
import com.flow.flowboard.authoring.repository.WorkflowRepository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service for authoring workflows.
 * Example:
 * <pre>
 *   Workflow created = workflowService.create("Name", "{}", "desc");
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public Workflow create(String name, String definitionJson, String description) {
        Workflow workflow = Workflow.builder()
                .name(name)
                .definitionJson(definitionJson)
                .description(description)
                .build();
        return workflowRepository.save(workflow);
    }

    public Workflow cloneOf(UUID sourceId, String newName) {
        Workflow source = workflowRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow not found: " + sourceId));
        Workflow clone = Workflow.builder()
                .name(newName != null ? newName : source.getName())
                .description(source.getDescription())
                .definitionJson(source.getDefinitionJson())
                .status(source.getStatus())
                .build();
        return workflowRepository.save(clone);
    }
}
