# Flowboard

Modular Spring Boot project:

- flow-common: shared beans (WebClient), DTOs, error handling
- flow-authoring-service: workflow CRUD & clone
- flow-ingestion-service: /api/v1/events (202 Accepted)
- flow-orchestration-service: /api/v1/workflows/{id}/execute
- flow-api-service: WebSockets (STOMP) for live updates

Java 21 virtual threads enabled. Use WebClient for HTTP.
