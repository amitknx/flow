package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.common.domain.NodeType;
import com.flow.flowboard.api.engine.queue.ExecutionTask;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Component
public class WaitNodeExecutor implements NodeExecutor {

    @Override
    public boolean supports(NodeType nodeType) {
        return NodeType.DELAY == nodeType;
    }

    @Override
    public Mono<Map<String, Object>> execute(ExecutionTask task) {
        // Delay is handled by the queue/orchestrator scheduling.
        // When this executes, the delay has already passed.
        return Mono.just(Collections.emptyMap());
    }
}
