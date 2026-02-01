package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.PlaygroundService;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteRequest;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for API Playground functionality
 * 
 * Allows users to execute arbitrary HTTP requests for testing APIs
 */
@RestController
@RequestMapping("/api/v1/playground")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlaygroundController {

    private final PlaygroundService playgroundService;

    /**
     * Execute an HTTP request from the playground
     *
     * @param request The execution request containing method, URL, headers, body,
     *                and timeout
     * @return The execution response with status, headers, body, and duration
     */
    @PostMapping("/execute")
    public Mono<ResponseEntity<PlaygroundExecuteResponse>> execute(
            @Valid @RequestBody PlaygroundExecuteRequest request) {
        return playgroundService.execute(request)
                .map(ResponseEntity::ok);
    }
}
