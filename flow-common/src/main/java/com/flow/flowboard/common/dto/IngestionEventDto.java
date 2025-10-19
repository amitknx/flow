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
package com.flow.flowboard.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for ingestion events.
 * Example:
 * <pre>
 *  IngestionEventDto dto = IngestionEventDto.builder()
 *      .workflowId("...uuid...")
 *      .correlationId("order-123")
 *      .occurredAt("2025-09-08T12:34:56Z")
 *      .build();
 * </pre>
 */
@Data
@Builder
public class IngestionEventDto {

    @NotBlank
    private String workflowId;

    @NotBlank
    private String correlationId;

    @NotBlank
    private String occurredAt;

    @NotNull
    private Step step;

    private Object input;
    private Object output;
    private Object error;

    @Data
    @Builder
    public static class Step {
        @NotBlank
        private String id;

        @NotBlank
        private String name;

        /** Optional: START, UPDATE, COMPLETE, FAIL */
        private String phase;
    }
}
