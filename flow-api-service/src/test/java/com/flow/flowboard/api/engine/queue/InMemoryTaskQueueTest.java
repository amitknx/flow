package com.flow.flowboard.api.engine.queue;

import com.flow.flowboard.common.domain.NodeType;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.UUID;

public class InMemoryTaskQueueTest {

    @Test
    public void testPushBeforeSubscribe() {
        InMemoryTaskQueue queue = new InMemoryTaskQueue();

        ExecutionTask task = new ExecutionTask(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test-node",
                NodeType.HTTP_REQUEST,
                Collections.emptyMap());

        System.out.println("Pushing task...");
        queue.push(task).block();
        System.out.println("Task pushed.");

        System.out.println("Subscribing to queue...");

        StepVerifier.create(queue.receive())
                .expectNextMatches(t -> t.nodeId().equals("test-node"))
                .thenCancel()
                .verify(java.time.Duration.ofSeconds(2));
    }
}
