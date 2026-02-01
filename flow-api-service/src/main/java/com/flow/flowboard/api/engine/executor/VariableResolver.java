package com.flow.flowboard.api.engine.executor;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariableResolver {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(.+?)\\}\\}");

    /**
     * Resolve variables in a string or object
     */
    public Object resolve(Object input, Map<String, Object> context) {
        if (input == null) {
            return null;
        }

        if (input instanceof String str) {
            return resolveString(str, context);
        } else if (input instanceof Map<?, ?> map) {
            Map<Object, Object> resolvedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                resolvedMap.put(entry.getKey(), resolve(entry.getValue(), context));
            }
            return resolvedMap;
        } else if (input instanceof List<?> list) {
            List<Object> resolvedList = new ArrayList<>();
            for (Object item : list) {
                resolvedList.add(resolve(item, context));
            }
            return resolvedList;
        }

        return input;
    }

    private Object resolveString(String input, Map<String, Object> context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(input);

        // If the entire string is a single variable, return the actual object type
        if (matcher.matches()) {
            return resolvePath(matcher.group(1), context);
        }

        // Otherwise, replace placeholders in the string
        StringBuilder sb = new StringBuilder();
        matcher.reset();
        while (matcher.find()) {
            Object resolved = resolvePath(matcher.group(1), context);
            matcher.appendReplacement(sb, resolved != null ? resolved.toString() : "");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Object resolvePath(String path, Map<String, Object> context) {
        // Convert context to JSON for JsonPath evaluation
        // This is a bit expensive, but ensures we can use standard JsonPath syntax
        try {
            // Prepend $. to the path if it's not already there
            String jsonPath = path.startsWith("$.") ? path : "$." + path;

            // Special handling for "steps.<node_id>.outputs" to match user syntax
            // The context already contains "steps" map if we implement it that way

            return JsonPath.read(context, jsonPath);
        } catch (PathNotFoundException e) {
            log.warn("Variable path not found: {}", path);
            return null;
        } catch (Exception e) {
            log.error("Error resolving variable path: {}", path, e);
            return null;
        }
    }
}
