package com.flow.flowboard.api.engine.service;

import com.flow.flowboard.api.engine.executor.NodeExecutor;
import com.flow.flowboard.api.engine.queue.TaskQueue;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskWorker {

    private final TaskQueue taskQueue;
    private final List<NodeExecutor> executors;
    private final WorkflowOrchestrator orchestrator;

    @PostConstruct
    public void start() {
        log.info("TaskWorker: Starting...");
        taskQueue.receive()
                .doOnSubscribe(s -> log.info("TaskWorker: Subscribed to queue"))
                .doOnNext(task -> log.info("TaskWorker: Received task {}", task.nodeId()))
                .flatMap(task -> {
                    // START and END nodes are placeholders - auto-complete them
                    if (task.nodeType() == com.flow.flowboard.common.domain.NodeType.START ||
                            task.nodeType() == com.flow.flowboard.common.domain.NodeType.END) {
                        log.info("Auto-completing {} node: {}", task.nodeType(), task.nodeId());
                        orchestrator.handleStepCompletion(task.stepId(), java.util.Collections.emptyMap());
                        return Mono.empty();
                    }

                    NodeExecutor executor = executors.stream()
                            .filter(e -> e.supports(task.nodeType()))
                            .findFirst()
                            .orElse(null);

                    if (executor == null) {
                        log.warn("No executor found for type: {}", task.nodeType());
                        return Mono.empty();
                    }

                    return executor.execute(task)
                            .doOnSuccess(result -> {
                                orchestrator.handleStepCompletion(task.stepId(), result);
                            })
                            .onErrorResume(e -> {
                                log.error("Task failed for step {}", task.stepId(), e);
                                orchestrator.handleStepFailure(task.stepId(), e.getMessage());
                                return Mono.empty();
                            });
                })
                .subscribe();
    }
}
