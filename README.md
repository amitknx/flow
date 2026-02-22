<div align="center">
  <h1>🌊 Flowboard</h1>
  <p><strong>A Modern, Scalable DAG Workflow Execution Engine built with Spring Boot</strong></p>
</div>

---

## 📖 Overview

**Flowboard** is a robust Directed Acyclic Graph (DAG) execution engine designed for orchestrating complex workflows. Built on top of **Spring Boot 3**, **Project Reactor (WebFlux)**, and **PostgreSQL**, Flowboard provides a highly performant and reliable backend for defining, executing, and monitoring multi-step workflows.

Whether you need to sequence HTTP API calls, execute custom Javascript scripts, or introduce conditional branching, Flowboard's swappable execution strategy makes it extensible and developer-friendly.

---

## ✨ Features

- **DAG Engine:** Define workflows as directed acyclic graphs, allowing for complex dependencies, parallel execution, and conditional logic.
- **Node Executors:** Out-of-the-box support for multiple node types:
  - 🌐 **HTTP Nodes:** Execute external API requests.
  - 📜 **Script Nodes:** Run dynamic Javascript code snippets securely using GraalVM.
  - ⏳ **Delay Nodes:** Introduce orchestrated pauses in your workflows.
  - 🔀 **Conditional Nodes:** Branch logic based on previous step outputs.
- **Dynamic Variable Resolution:** Seamlessly pass data between steps using JSONPath syntax (e.g., `{{steps.nodeA.outputs.data.id}}`).
- **Reactive Stack:** Built with Spring WebFlux for non-blocking, high-concurrency API integrations.
- **Swappable Task Queue:** Currently utilizes a highly efficient in-memory task queue, designed with an interface that easily allows swapping to RabbitMQ or Apache Kafka for distributed deployments.
- **Robust Persistence:** State management and audit logging backed by PostgreSQL and Flyway migrations.
- **API Playground:** Built-in controller to test and validate node configurations before deploying them into full workflows.
- **WebSockets:** Live execution updates streamed via WebSockets (STOMP).
- **Virtual Threads:** Fully leverages Java 21 Virtual Threads natively where applicable.

---

## 🏗️ Architecture

The project consists of two primary modules:

1. **`flow-common`**: Contains shared DTOs, domain models (e.g., `ExecutionStatus`, `StepStatus`), global configurations (e.g., `WebClientConfig`), and basic error handling logic.
2. **`flow-api-service`**: The core execution engine. Contains:
   - **Engine API (`/api/v1/...`)**: Exposes REST endpoints for managing Workflows, API Endpoints, Secrets, Executions, and a Dashboard.
   - **Orchestrator**: The `WorkflowOrchestrator` handles DAG traversal and schedules tasks.
   - **Queue**: `InMemoryTaskQueue` and `TaskWorker` handle async node execution.
   - **Executors**: The Strategy pattern implementation that routes tasks to `HttpNodeExecutor`, `ScriptNodeExecutor`, etc.
   - **Resolver**: `VariableResolver` interpolates output context into downstream node configurations.

*(Note: Older microservice modules like flow-authoring-service, flow-ingestion-service, and flow-orchestration-service have been consolidated into `flow-api-service` to streamline the architecture.)*

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **PostgreSQL 15+**

### Local Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/flow.git
   cd flow
   ```

2. **Configure Database Settings**
   Ensure PostgreSQL is running locally on port `5432`. Create a database named `flowboard`.
   Update your database credentials in `flow-api-service/src/main/resources/application.yml` if necessary.

3. **Build the Project**
   ```bash
   mvn clean install -DskipTests
   ```

4. **Run the API Service**
   ```bash
   cd flow-api-service
   mvn spring-boot:run
   ```
   
   The server will start on `http://localhost:8080`. Flyway will automatically run the schema migrations upon startup.

---

## 📚 API Endpoints

Flowboard provides a comprehensive REST API. Some of the core endpoints include:

- `GET /api/v1/dashboard/stats` - Retrieve high-level execution metrics.
- `POST /api/v1/workflows` - Create a new DAG workflow definition.
- `POST /api/v1/executions` - Trigger a workflow execution.
- `POST /api/v1/playground/execute` - Test a single node configuration instantly.

*Swagger/OpenAPI documentation is available at `/swagger-ui.html` when running locally.*

---

## 🧪 Testing

To run the unit and integration test suites:

```bash
mvn test
```

Tests include comprehensive coverage for engine components like the `InMemoryTaskQueueTest`, `VariableResolverTest`, and `ScriptNodeExecutorTest`.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
