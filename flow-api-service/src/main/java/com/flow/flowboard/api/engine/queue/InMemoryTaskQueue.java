package com.flow.flowboard.api.engine.queue;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Component
public class InMemoryTaskQueue implements TaskQueue {

    private final Sinks.Many<ExecutionTask> sink;

    public InMemoryTaskQueue() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public Mono<Void> push(ExecutionTask task) {
        return Mono.fromRunnable(() -> {
            Sinks.EmitResult result = sink.tryEmitNext(task);
            if (result.isFailure()) {
                throw new RuntimeException("Failed to emit task: " + result);
            }
        });
    }

    @Override
    public Mono<Void> pushDelayed(ExecutionTask task, long delayMillis) {
        return Mono.delay(Duration.ofMillis(delayMillis))
                .then(push(task));
    }

    @Override
    public Flux<ExecutionTask> receive() {
        return sink.asFlux();
    }
}
