package com.flow.flowboard.api.engine.service;

import com.flow.flowboard.api.engine.executor.NodeExecutor;
import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.api.engine.queue.TaskQueue;
import com.flow.flowboard.common.domain.NodeType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TaskWorkerIntegrationTest {

    @Autowired
    private TaskQueue taskQueue;

    @MockBean
    private WorkflowOrchestrator orchestrator;

    @MockBean
    private NodeExecutor nodeExecutor;

    @Test
    public void testTaskProcessing() throws InterruptedException {
        // Setup mock
        when(nodeExecutor.supports(NodeType.HTTP_REQUEST)).thenReturn(true);
        CountDownLatch latch = new CountDownLatch(1);
        when(nodeExecutor.execute(any(ExecutionTask.class))).thenAnswer(invocation -> {
            System.out.println("MockExecutor: Executing task");
            latch.countDown();
            return Mono.just(Map.of("status", "success"));
        });

        // Push task
        ExecutionTask task = new ExecutionTask(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test-node",
                NodeType.HTTP_REQUEST,
                Collections.emptyMap());

        System.out.println("Test: Pushing task...");
        taskQueue.push(task).block();

        // Wait for processing
        boolean processed = latch.await(5, TimeUnit.SECONDS);
        if (processed) {
            System.out.println("SUCCESS: Task processed by worker.");
        } else {
            System.out.println("FAILURE: Task NOT processed by worker within timeout.");
        }
    }
}
