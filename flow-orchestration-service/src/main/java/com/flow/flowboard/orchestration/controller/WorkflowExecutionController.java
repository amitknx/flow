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
package com.flow.flowboard.orchestration.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Execution controller.
 */
@RestController
@RequestMapping("/api/v1")
public class WorkflowExecutionController {

    @PostMapping("/workflows/{id}/execute")
    public ResponseEntity<Map<String, String>> execute(@PathVariable("id") String id, @RequestBody(required = false) Map<String, Object> input) {
        String executionId = UUID.randomUUID().toString();
        return ResponseEntity.ok(Map.of("executionId", executionId, "workflowId", id));
    }
}
