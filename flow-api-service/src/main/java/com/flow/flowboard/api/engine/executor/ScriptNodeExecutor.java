package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.common.domain.NodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptNodeExecutor implements NodeExecutor {

    private final VariableResolver variableResolver;

    @Override
    public boolean supports(NodeType nodeType) {
        return NodeType.SCRIPT == nodeType;
    }

    @Override
    public Mono<Map<String, Object>> execute(ExecutionTask task) {
        return Mono.fromCallable(() -> {
            Map<String, Object> contextData = task.context();
            String script = (String) contextData.get("code");

            if (script == null || script.isBlank()) {
                throw new IllegalArgumentException("Script code is required for SCRIPT node (key: 'code')");
            }

            // Resolve variables in the script code
            Object resolvedScriptObj = variableResolver.resolve(script, contextData);
            String resolvedScript = resolvedScriptObj != null ? resolvedScriptObj.toString() : script;

            log.info("Executing script for node {}: {}", task.nodeId(), resolvedScript);

            // Wrap script in a function to support 'return' statement
            String wrappedScript = "(function() {\n" + resolvedScript + "\n})()";

            try (Context polyglotContext = Context.create("js")) {
                Value result = polyglotContext.eval("js", wrappedScript);

                Map<String, Object> output = new HashMap<>();
                output.put("result", convertValue(result));
                return output;
            } catch (Exception e) {
                log.error("Error executing script for node {}", task.nodeId(), e);
                throw new RuntimeException("Script execution failed: " + e.getMessage(), e);
            }
        });
    }

    private Object convertValue(Value value) {
        if (value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            if (value.fitsInInt()) {
                return value.asInt();
            } else if (value.fitsInLong()) {
                return value.asLong();
            } else {
                return value.asDouble();
            }
        } else if (value.isString()) {
            return value.asString();
        } else if (value.hasArrayElements()) {
            int size = (int) value.getArraySize();
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++) {
                array[i] = convertValue(value.getArrayElement(i));
            }
            return array;
        } else if (value.hasMembers()) {
            Map<String, Object> map = new HashMap<>();
            for (String key : value.getMemberKeys()) {
                map.put(key, convertValue(value.getMember(key)));
            }
            return map;
        }
        return value.toString();
    }
}
