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
package com.flow.flowboard.ingestion.controller;

import com.flow.flowboard.common.dto.IngestionEventDto;
import com.flow.flowboard.ingestion.service.IngestionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ingestion endpoints.
 * Example:
 *  POST /api/v1/events
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WorkflowEventController {

    private final IngestionService ingestionService;

    @PostMapping("/events")
    public ResponseEntity<Void> ingestSingle(@Validated @RequestBody IngestionEventDto event) {
        ingestionService.ingest(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/events/bulk")
    public ResponseEntity<Void> ingestBulk(@Validated @RequestBody List<IngestionEventDto> events) {
        for (IngestionEventDto e : events) {
            ingestionService.ingest(e);
        }
        return ResponseEntity.accepted().build();
    }
}
