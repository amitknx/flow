package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.common.domain.NodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for CONDITION nodes.
 * 
 * Evaluates the condition expression against the context (previous node
 * outputs)
 * and returns the result (true/false) to be used for routing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalNodeExecutor implements NodeExecutor {

    private final ConditionExpressionEvaluator expressionEvaluator;

    @Override
    public boolean supports(NodeType nodeType) {
        return NodeType.CONDITIONAL == nodeType;
    }

    @Override
    public Mono<Map<String, Object>> execute(ExecutionTask task) {
        return Mono.fromCallable(() -> {
            Map<String, Object> context = task.context();

            // Get the expression from node data
            String condition = null;
            if (context.containsKey("condition")) {
                condition = context.get("condition").toString();
            }

            if (condition == null || condition.isBlank()) {
                log.error("No expression found for CONDITION node: {}", task.nodeId());
                Map<String, Object> result = new HashMap<>();
                result.put("conditionResult", false);
                result.put("error", "Missing expression");
                return result;
            }

            log.info("Evaluating condition: {}", condition);

            // Evaluate the expression
            boolean conditionResult = expressionEvaluator.evaluate(condition, context);

            log.info("Condition result for node {}: {}", task.nodeId(), conditionResult);

            Map<String, Object> result = new HashMap<>();
            result.put("conditionResult", conditionResult);
            result.put("condition", condition);
            return result;
        });
    }
}
