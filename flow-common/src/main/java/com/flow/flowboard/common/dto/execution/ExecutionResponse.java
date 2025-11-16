package com.flow.flowboard.common.dto.execution;

import com.flow.flowboard.common.domain.ExecutionStatus;
import com.flow.flowboard.common.domain.TriggerType;
import com.flow.flowboard.common.dto.workflow.WorkflowDefinitionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {

    private UUID id;
    private UUID workflowId;
    private String correlationId;
    private ExecutionStatus status;
    private TriggerType triggerType;
    private Object triggerInput;
    private WorkflowDefinitionDto definitionSnapshot;
    private Instant startedAt;
    private Instant endedAt;
    private Boolean active;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
    private List<StepExecutionResponse> stepExecutions;
}
