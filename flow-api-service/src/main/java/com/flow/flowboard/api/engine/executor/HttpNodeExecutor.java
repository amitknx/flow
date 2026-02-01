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
    private final VariableResolver variableResolver;

    @Override
    public boolean supports(NodeType nodeType) {
        return NodeType.HTTP_REQUEST == nodeType;
    }

    @Override
    public Mono<Map<String, Object>> execute(ExecutionTask task) {
        Map<String, Object> context = task.context();

        // Extract apiEndpointId from context
        String endpointIdStr = (String) context.get("endpointId");
        if (endpointIdStr == null) {
            return Mono.error(new IllegalArgumentException("apiEndpointId is required for HTTP_REQUEST node"));
        }
        UUID endpointId = UUID.fromString(endpointIdStr);

        // Build request from context (optional overrides)
        ExecuteEndpointRequest.ExecuteEndpointRequestBuilder requestBuilder = ExecuteEndpointRequest.builder();

        if (context.containsKey("headers") && context.get("headers") instanceof Map) {
            Map<String, String> resolvedHeaders = (Map<String, String>) variableResolver.resolve(context.get("headers"),
                    context);
            requestBuilder.headers(resolvedHeaders);
        }

        if (context.containsKey("queryParams") && context.get("queryParams") instanceof Map) {
            Map<String, String> resolvedParams = (Map<String, String>) variableResolver
                    .resolve(context.get("queryParams"), context);
            requestBuilder.queryParams(resolvedParams);
        }

        if (context.containsKey("pathParams") && context.get("pathParams") instanceof Map) {
            Map<String, String> resolvedPathParams = (Map<String, String>) variableResolver
                    .resolve(context.get("pathParams"), context);
            requestBuilder.pathParams(resolvedPathParams);
        }

        if (context.containsKey("requestBody")) {
            Object resolvedBody = variableResolver.resolve(context.get("requestBody"), context);
            requestBuilder.requestBody(resolvedBody);
        }

        ExecuteEndpointRequest request = requestBuilder.build();

        return apiExecutorService.executeEndpoint(endpointId, request)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("response", response);
                    return result;
                });
    }
}
