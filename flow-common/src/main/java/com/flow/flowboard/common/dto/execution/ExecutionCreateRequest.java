package com.flow.flowboard.common.dto.execution;

import com.flow.flowboard.common.domain.TriggerType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionCreateRequest {

    @NotNull(message = "Workflow ID is required")
    private UUID workflowId;

    @NotNull(message = "Trigger type is required")
    private TriggerType triggerType;

    private Object triggerInput;

    @Size(max = 255, message = "Correlation ID must not exceed 255 characters")
    private String correlationId;
}
