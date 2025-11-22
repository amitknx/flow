package com.flow.flowboard.api.engine.queue;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskQueue {
    Mono<Void> push(ExecutionTask task);

    Mono<Void> pushDelayed(ExecutionTask task, long delayMillis);

    Flux<ExecutionTask> receive();
}
