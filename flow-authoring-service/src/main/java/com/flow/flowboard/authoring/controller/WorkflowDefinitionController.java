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
package com.flow.flowboard.authoring.controller;

import com.flow.flowboard.authoring.domain.Workflow;
import com.flow.flowboard.authoring.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Workflow definition endpoints.
 * Example:
 *  POST /api/v1/workflows
 */
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowDefinitionController {

    private final WorkflowService workflowService;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.getOrDefault("name", "Untitled Workflow");
        String description = (String) body.getOrDefault("description", null);
        Object definition = body.getOrDefault("definition", Map.of("nodes", java.util.List.of(), "edges", java.util.List.of()));
        String definitionJson = objectMapper.valueToTree(definition).toString();
        Workflow created = workflowService.create(name, definitionJson, description);
        return ResponseEntity.ok(Map.of("workflowId", created.getId().toString()));
    }

    @PostMapping("/{id}/clone")
    public ResponseEntity<Map<String, String>> clone(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> body) {
        String newName = body != null ? (String) body.get("name") : null;
        Workflow cloned = workflowService.cloneOf(UUID.fromString(id), newName);
        return ResponseEntity.ok(Map.of("workflowId", cloned.getId().toString(), "clonedFrom", id));
    }
}
