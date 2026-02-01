package com.flow.flowboard.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteRequest;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PlaygroundServiceTest {

    private MockWebServer mockWebServer;
    private PlaygroundService playgroundService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        WebClient.Builder webClientBuilder = WebClient.builder();
        playgroundService = new PlaygroundService(webClientBuilder, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testExecuteGetRequest() {
        String responseBody = "{\"message\": \"Hello World\", \"id\": 123}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("GET")
                .url(mockWebServer.url("/api/test").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(200, response.getStatus());
                    assertEquals("OK", response.getStatusText());
                    assertNotNull(response.getHeaders());
                    assertNotNull(response.getBody());
                    assertTrue(response.getDurationMs() >= 0);
                    assertNull(response.getError());

                    // Verify body was parsed as JSON
                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = (Map<String, Object>) response.getBody();
                    assertEquals("Hello World", body.get("message"));
                    assertEquals(123, body.get("id"));
                })
                .verifyComplete();
    }

    @Test
    void testExecutePostRequestWithBody() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"success\": true}")
                .setHeader("Content-Type", "application/json")
                .setResponseCode(201));

        Map<String, Object> requestBody = Map.of("name", "Test", "value", 42);

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("POST")
                .url(mockWebServer.url("/api/create").toString())
                .headers(Map.of("X-Custom-Header", "custom-value"))
                .body(requestBody)
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(201, response.getStatus());
                    assertEquals("Created", response.getStatusText());
                })
                .verifyComplete();

        // Verify request was sent correctly
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("custom-value", recordedRequest.getHeader("X-Custom-Header"));
        assertTrue(recordedRequest.getHeader("Content-Type").contains("application/json"));
    }

    @Test
    void testExecuteWithHeaders() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{}")
                .setResponseCode(200));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("GET")
                .url(mockWebServer.url("/api/test").toString())
                .headers(Map.of(
                        "Authorization", "Bearer token123",
                        "Accept", "application/json"))
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> assertEquals(200, response.getStatus()))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("Bearer token123", recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
    }

    @Test
    void testExecuteWith404Response() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"error\": \"Not found\"}")
                .setResponseCode(404));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("GET")
                .url(mockWebServer.url("/api/notfound").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(404, response.getStatus());
                    assertEquals("Not Found", response.getStatusText());
                    assertNull(response.getError());
                })
                .verifyComplete();
    }

    @Test
    void testExecuteWithPlainTextResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("Plain text response")
                .setHeader("Content-Type", "text/plain")
                .setResponseCode(200));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("GET")
                .url(mockWebServer.url("/api/text").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(200, response.getStatus());
                    assertEquals("Plain text response", response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void testExecutePutRequest() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"updated\": true}")
                .setResponseCode(200));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("PUT")
                .url(mockWebServer.url("/api/update/1").toString())
                .body(Map.of("name", "Updated"))
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> assertEquals(200, response.getStatus()))
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("PUT", recordedRequest.getMethod());
    }

    @Test
    void testExecuteDeleteRequest() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("DELETE")
                .url(mockWebServer.url("/api/delete/1").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(204, response.getStatus());
                    assertEquals("No Content", response.getStatusText());
                })
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void testExecuteWithEmptyBody() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("DELETE")
                .url(mockWebServer.url("/api/delete").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(204, response.getStatus());
                    assertNull(response.getBody());
                })
                .verifyComplete();
    }

    @Test
    void testExecuteWithJsonArray() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("[{\"id\": 1}, {\"id\": 2}]")
                .setHeader("Content-Type", "application/json")
                .setResponseCode(200));

        PlaygroundExecuteRequest request = PlaygroundExecuteRequest.builder()
                .method("GET")
                .url(mockWebServer.url("/api/list").toString())
                .timeoutMs(5000)
                .build();

        StepVerifier.create(playgroundService.execute(request))
                .assertNext(response -> {
                    assertEquals(200, response.getStatus());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody() instanceof java.util.List);
                })
                .verifyComplete();
    }
}
