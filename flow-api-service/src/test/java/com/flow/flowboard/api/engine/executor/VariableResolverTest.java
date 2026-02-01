package com.flow.flowboard.api.engine.executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableResolverTest {

    private VariableResolver variableResolver;
    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        variableResolver = new VariableResolver();
        context = new HashMap<>();

        Map<String, Object> step1Output = new HashMap<>();
        step1Output.put("id", 123);
        step1Output.put("name", "Test User");

        Map<String, Object> step1 = new HashMap<>();
        step1.put("outputs", step1Output);

        Map<String, Object> steps = new HashMap<>();
        steps.put("node1", step1);

        context.put("steps", steps);
        context.put("output", step1Output); // Also test direct output
    }

    @Test
    void testResolveSingleVariable() {
        String input = "{{steps.node1.outputs.id}}";
        Object result = variableResolver.resolve(input, context);
        assertEquals(123, result);
    }

    @Test
    void testResolveStringWithVariables() {
        String input = "User {{steps.node1.outputs.name}} has ID {{steps.node1.outputs.id}}";
        Object result = variableResolver.resolve(input, context);
        assertEquals("User Test User has ID 123", result);
    }

    @Test
    void testResolveMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("userId", "{{steps.node1.outputs.id}}");
        input.put("userName", "{{steps.node1.outputs.name}}");
        input.put("static", "value");

        Object result = variableResolver.resolve(input, context);
        assertTrue(result instanceof Map);
        Map<?, ?> resolved = (Map<?, ?>) result;
        assertEquals(123, resolved.get("userId"));
        assertEquals("Test User", resolved.get("userName"));
        assertEquals("value", resolved.get("static"));
    }

    @Test
    void testResolveList() {
        List<Object> input = List.of("{{steps.node1.outputs.id}}", "static");
        Object result = variableResolver.resolve(input, context);
        assertTrue(result instanceof List);
        List<?> resolved = (List<?>) result;
        assertEquals(123, resolved.get(0));
        assertEquals("static", resolved.get(1));
    }

    @Test
    void testResolveMissingPath() {
        String input = "{{steps.node1.outputs.missing}}";
        Object result = variableResolver.resolve(input, context);
        assertNull(result);
    }

    @Test
    void testResolveMixedString() {
        String input = "ID: {{steps.node1.outputs.id}}";
        Object result = variableResolver.resolve(input, context);
        assertEquals("ID: 123", result);
    }
}
