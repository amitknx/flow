package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.common.domain.NodeType;
import com.flow.flowboard.api.engine.queue.ExecutionTask;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface NodeExecutor {
    boolean supports(NodeType nodeType);

    Mono<Map<String, Object>> execute(ExecutionTask task);
}
