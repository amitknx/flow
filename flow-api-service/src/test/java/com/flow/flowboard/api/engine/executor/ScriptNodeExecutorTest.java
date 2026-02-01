package com.flow.flowboard.api.engine.executor;

import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.common.domain.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ScriptNodeExecutorTest {

    private ScriptNodeExecutor scriptNodeExecutor;

    @Mock
    private VariableResolver variableResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scriptNodeExecutor = new ScriptNodeExecutor(variableResolver);
    }

    @Test
    void testExecuteSimpleScript() {
        Map<String, Object> context = new HashMap<>();
        context.put("code", "return 1 + 1;");

        when(variableResolver.resolve(anyString(), anyMap())).thenAnswer(i -> i.getArgument(0));

        ExecutionTask task = new ExecutionTask(UUID.randomUUID(), UUID.randomUUID(), "node1", NodeType.SCRIPT, context);

        StepVerifier.create(scriptNodeExecutor.execute(task))
                .assertNext(result -> {
                    assertEquals(2, result.get("result"));
                })
                .verifyComplete();
    }

    @Test
    void testExecuteScriptWithVariables() {
        Map<String, Object> context = new HashMap<>();
        context.put("script", "return {{steps.node1.outputs.val}} + 10;");

        // Mock variable resolver to replace the placeholder with "5"
        when(variableResolver.resolve(anyString(), anyMap())).thenReturn("return 5 + 10;");

        ExecutionTask task = new ExecutionTask(UUID.randomUUID(), UUID.randomUUID(), "node1", NodeType.SCRIPT, context);

        StepVerifier.create(scriptNodeExecutor.execute(task))
                .assertNext(result -> {
                    assertEquals(15, result.get("result"));
                })
                .verifyComplete();
    }

    @Test
    void testExecuteScriptReturningObject() {
        Map<String, Object> context = new HashMap<>();
        context.put("script", "return { a: 1, b: 'test', c: [1, 2] };");

        when(variableResolver.resolve(anyString(), anyMap())).thenAnswer(i -> i.getArgument(0));

        ExecutionTask task = new ExecutionTask(UUID.randomUUID(), UUID.randomUUID(), "node1", NodeType.SCRIPT, context);

        StepVerifier.create(scriptNodeExecutor.execute(task))
                .assertNext(result -> {
                    Map<String, Object> scriptResult = (Map<String, Object>) result.get("result");
                    assertEquals(1, scriptResult.get("a"));
                    assertEquals("test", scriptResult.get("b"));
                    Object[] array = (Object[]) scriptResult.get("c");
                    assertEquals(2, array.length);
                    assertEquals(1, array[0]);
                    assertEquals(2, array[1]);
                })
                .verifyComplete();
    }

    @Test
    void testExecuteScriptWithComplexLogic() {
        String script = """
                var a = 'hello';
                var b = ' world!';
                return {
                    'a' : a,
                    'b': b,
                    'c' : a + b,
                    'd' : [1, 2, 3, 4]
                };
                """;
        Map<String, Object> context = new HashMap<>();
        context.put("script", script);

        when(variableResolver.resolve(anyString(), anyMap())).thenAnswer(i -> i.getArgument(0));

        ExecutionTask task = new ExecutionTask(UUID.randomUUID(), UUID.randomUUID(), "node1", NodeType.SCRIPT, context);

        StepVerifier.create(scriptNodeExecutor.execute(task))
                .assertNext(result -> {
                    Map<String, Object> scriptResult = (Map<String, Object>) result.get("result");
                    assertEquals("hello", scriptResult.get("a"));
                    assertEquals(" world!", scriptResult.get("b"));
                    assertEquals("hello world!", scriptResult.get("c"));
                    Object[] array = (Object[]) scriptResult.get("d");
                    assertEquals(4, array.length);
                })
                .verifyComplete();
    }

    @Test
    void testScriptError() {
        Map<String, Object> context = new HashMap<>();
        context.put("script", "return undefinedVariable;");

        when(variableResolver.resolve(anyString(), anyMap())).thenAnswer(i -> i.getArgument(0));

        ExecutionTask task = new ExecutionTask(UUID.randomUUID(), UUID.randomUUID(), "node1", NodeType.SCRIPT, context);

        StepVerifier.create(scriptNodeExecutor.execute(task))
                .expectErrorMatches(throwable -> throwable.getMessage().contains("Script execution failed"))
                .verify();
    }
}
