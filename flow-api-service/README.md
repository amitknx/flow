# Flow API Service

Part of the **Flowboard** platform - a visualization-first workflow platform for real-time monitoring and orchestration.

## Overview

The **Flow API Service** is responsible for:
- **WebSocket communication**: Real-time updates for workflow execution status via STOMP/SockJS
- **API Gateway**: Serves as the primary entry point for the React UI
- **Live execution broadcasting**: Pushes execution updates to connected clients

This service acts as the communication bridge between the backend services (authoring, ingestion, orchestration) and the frontend UI.

## Architecture Context

### Flowboard Platform Structure

```
flowboard/
├── flow-common/                    # Shared utilities, DTOs, error handling
├── flow-authoring-service/         # Workflow CRUD & clone operations
├── flow-ingestion-service/         # Event ingestion from external services
├── flow-orchestration-service/     # Workflow execution engine
└── flow-api-service/              # WebSocket broker + API gateway (THIS SERVICE)
```

### Flowboard's Dual Purpose

1. **Active Flow Orchestration**:
   - Users author workflows in the UI (nodes + edges)
   - Execute workflows via API/UI triggers
   - Monitor execution in real-time with data/status

2. **Passive Flow Observability**:
   - External services publish execution events
   - Flowboard collects and visualizes the data
   - Shows execution sequence, duration, timing, data flow
   - Acts as observability layer for distributed systems

## Technology Stack

- **Java 21** with virtual threads enabled
- **Spring Boot 3.3.3**
- **Spring WebSocket** with STOMP messaging
- **SockJS** for WebSocket fallback support
- **Lombok** for reducing boilerplate
- **Maven** for dependency management

## Key Components

### WebSocket Configuration

Located in `config.com.flow.flowboard.api.WebSocketConfig`

- **Endpoint**: `/ws` (with SockJS fallback)
- **Message Broker**: Simple in-memory broker with `/topic` prefix
- **Application Prefix**: `/app`
- **CORS**: Configured with `allowedOriginPatterns("*")` for development

### WebSocket Topics

Clients subscribe to real-time updates via:

```
/topic/workflows/{workflowId}/executions/{executionId}
/topic/workflows/{workflowId}
```

Messages pushed include:
- Step execution status changes (PENDING → RUNNING → SUCCESS/FAILED)
- Node input/output payloads
- Execution timing and duration
- Error information

## Configuration

### application.yml

```yaml
spring:
  threads:
    virtual:
      enabled: true  # Java 21 virtual threads for better concurrency
```

### Environment Variables

Configure service connectivity and ports (typically via Spring profiles):

```bash
# Server Port
SERVER_PORT=8080

# Database Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/flowboard
SPRING_DATASOURCE_USERNAME=flowboard_user
SPRING_DATASOURCE_PASSWORD=flowboard_pass

# Message Queue (for inter-service communication)
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
```

## API Endpoints

While the primary focus is WebSocket communication, this service may also expose:

### WebSocket Endpoints

- `GET /ws` - WebSocket handshake (STOMP)
- `GET /ws/**` - SockJS fallback endpoints

### Health & Monitoring

- `GET /actuator/health` - Service health check
- `GET /actuator/info` - Service information

## Development

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 14+ (for shared database)
- RabbitMQ (for inter-service messaging)

### Building

```bash
# From project root
mvn clean install

# Build only this service
cd flow-api-service
mvn clean package
```

### Running

```bash
# From project root
mvn spring-boot:run -pl flow-api-service

# Or run the JAR
java -jar flow-api-service/target/flow-api-service-0.1.0-SNAPSHOT.jar
```

Service starts on port `8080` by default.

### Testing WebSocket Connection

Using a WebSocket client or the frontend UI:

```javascript
// Connect via SockJS
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
  console.log('Connected: ' + frame);

  // Subscribe to execution updates
  stompClient.subscribe('/topic/workflows/123/executions/456', function(message) {
    console.log('Received:', JSON.parse(message.body));
  });
});
```

## Integration with Other Services

### Flow Ingestion Service
- Receives events from external services via `/api/v1/events`
- Processes events and broadcasts updates via WebSocket (through this service)

### Flow Orchestration Service
- Executes UI-authored workflows
- Emits step execution events
- Events are broadcast to connected clients via WebSocket

### Flow Authoring Service
- Manages workflow definitions
- Changes may trigger notifications to subscribed clients

## Data Models

### WebSocket Message Format

Execution update messages follow this structure:

```json
{
  "executionId": "uuid",
  "workflowId": "uuid",
  "correlationId": "unique-correlation-id",
  "status": "RUNNING",
  "step": {
    "nodeId": "node-123",
    "nodeName": "HTTP Request to API",
    "status": "SUCCESS",
    "startedAt": "2024-01-15T10:30:00Z",
    "endedAt": "2024-01-15T10:30:02Z",
    "durationMs": 2000,
    "input": { "url": "https://api.example.com" },
    "output": { "status": 200, "body": "..." },
    "error": null
  }
}
```

## Engineering Standards

Following Flowboard platform conventions:

- **Beans over static utilities**: Dependency injection preferred
- **Lombok**: Used for DTOs, entities, and configuration classes
- **Error Handling**: Problem+JSON format (from flow-common)
- **Observability**:
  - OpenTelemetry for distributed tracing
  - Micrometer for metrics
  - Include `correlationId` in all logs/traces

## Security

(Planned for MVP+):
- **Authentication**: JWT-based via Spring Security
- **Authorization**: RBAC with roles (Admin/Editor/Viewer)
- **WebSocket Security**: Session validation for topic subscriptions

## Monitoring & Observability

### Metrics to Monitor

- Active WebSocket connections
- Message throughput (messages/sec)
- Subscription count per topic
- Message delivery latency

### Logging

Use structured logging with correlation IDs:

```java
log.info("Broadcasting execution update - correlationId: {}, executionId: {}",
  correlationId, executionId);
```

## Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
COPY target/flow-api-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

Key considerations:
- **Session affinity**: Required for WebSocket connections
- **Horizontal scaling**: Use Redis-backed message broker for multi-instance deployments
- **Health probes**: Configure liveness/readiness endpoints

## Future Enhancements

- [ ] Redis-backed STOMP message broker for horizontal scaling
- [ ] WebSocket session management and reconnection logic
- [ ] Message compression for large payloads
- [ ] Rate limiting per client connection
- [ ] Binary message support for performance
- [ ] GraphQL subscriptions as alternative to WebSocket

## Related Documentation

- [Flowboard Specification](../FLOWBOARD_SPEC.md) - Complete platform specification
- [Flow Common](../flow-common/README.md) - Shared utilities and DTOs
- [Flow Ingestion Service](../flow-ingestion-service/README.md) - Event ingestion
- [Flow Orchestration Service](../flow-orchestration-service/README.md) - Execution engine
- [Flowboard UI](../../flow-ui/README.md) - React frontend

## Support

For issues or questions:
- Check the [FLOWBOARD_SPEC.md](../FLOWBOARD_SPEC.md) for architectural decisions
- Review existing issues in the project tracker
- Contact the Flowboard team

---

**Package**: `com.flow.flowboard.api`
**Version**: 0.1.0-SNAPSHOT
**Java Version**: 21
**Spring Boot**: 3.3.3
