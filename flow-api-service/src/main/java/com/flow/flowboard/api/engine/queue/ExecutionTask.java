package com.flow.flowboard.api.engine.queue;

import com.flow.flowboard.common.domain.NodeType;
import java.util.Map;
import java.util.UUID;

public record ExecutionTask(
    UUID executionId,
    UUID stepId,
    String nodeId,
    NodeType nodeType,
    Map<String, Object> context
) {}
