package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.api.service.ApiExecutorService;
import com.flow.flowboard.common.domain.NodeType;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HttpNodeExecutor implements NodeExecutor {

    private final ApiExecutorService apiExecutorService;

    @Override
    public boolean supports(NodeType nodeType) {
        return NodeType.HTTP_REQUEST == nodeType;
    }

    @Override
    public Mono<Map<String, Object>> execute(ExecutionTask task) {
        Map<String, Object> context = task.context();

        // Extract apiEndpointId from context
        String endpointIdStr = (String) context.get("apiEndpointId");
        if (endpointIdStr == null) {
            return Mono.error(new IllegalArgumentException("apiEndpointId is required for HTTP_REQUEST node"));
        }
        UUID endpointId = UUID.fromString(endpointIdStr);

        // Build request from context (optional overrides)
        ExecuteEndpointRequest request = ExecuteEndpointRequest.builder()
                // TODO: Map other fields from context if needed (params, body, etc)
                .build();

        return apiExecutorService.executeEndpoint(endpointId, request)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("response", response);
                    return result;
                });
    }
}
