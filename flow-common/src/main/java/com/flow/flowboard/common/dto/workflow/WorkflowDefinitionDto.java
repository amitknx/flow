package com.flow.flowboard.common.dto.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinitionDto {
    private List<String> tags;
    private List<EdgeDto> edges;
    private List<NodeDto> nodes;
    private Map<String, Object> globals;
}
