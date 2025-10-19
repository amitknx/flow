# Quick Start - Workflow APIs

## 1️⃣ Database Setup (One-time)

```bash
# Create database and user
psql -U postgres
```

```sql
CREATE USER flowboard_user WITH PASSWORD 'flowboard_pass';
CREATE DATABASE flowboard OWNER flowboard_user;
GRANT ALL PRIVILEGES ON DATABASE flowboard TO flowboard_user;
\c flowboard
GRANT ALL ON SCHEMA public TO flowboard_user;
\q
```

## 2️⃣ Build & Run

```bash
cd /Users/amit/dev/flow
mvn clean install
mvn spring-boot:run -pl flow-api-service
```

## 3️⃣ Test APIs

### Create Workflow
```bash
curl -X POST http://localhost:8080/api/v1/workflows \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Order Processing Flow",
    "description": "Handles customer orders",
    "data": {
      "nodes": [
        {"id": "1", "type": "START", "position": {"x": 100, "y": 100}},
        {"id": "2", "type": "END", "position": {"x": 300, "y": 100}}
      ],
      "edges": [
        {"id": "e1", "source": "1", "target": "2"}
      ]
    }
  }'
```

### Get All Workflows
```bash
curl http://localhost:8080/api/v1/workflows
```

### Get Workflow by ID
```bash
curl http://localhost:8080/api/v1/workflows/{id}
```

### Search Workflows
```bash
curl http://localhost:8080/api/v1/workflows?search=order
```

### Update Workflow
```bash
curl -X PUT http://localhost:8080/api/v1/workflows/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Name",
    "description": "Updated description"
  }'
```

### Delete Workflow (Soft Delete)
```bash
curl -X DELETE http://localhost:8080/api/v1/workflows/{id}
```

### Clone Workflow
```bash
curl -X POST http://localhost:8080/api/v1/workflows/{id}/clone \
  -H "Content-Type: application/json" \
  -d '{"name": "Cloned Workflow"}'
```

## 4️⃣ Pagination

```bash
# Page 2, 10 items per page
curl "http://localhost:8080/api/v1/workflows?page=1&size=10"

# Sort by name
curl "http://localhost:8080/api/v1/workflows?sort=name,asc"

# Sort by creation date (newest first)
curl "http://localhost:8080/api/v1/workflows?sort=createdAt,desc"
```

## 5️⃣ Health Check

```bash
curl http://localhost:8080/actuator/health
```

## 📋 API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/workflows` | Create workflow |
| GET | `/api/v1/workflows` | List workflows (paginated) |
| GET | `/api/v1/workflows?search=term` | Search workflows |
| GET | `/api/v1/workflows/{id}` | Get workflow by ID |
| PUT | `/api/v1/workflows/{id}` | Update workflow |
| DELETE | `/api/v1/workflows/{id}` | Soft delete workflow |
| POST | `/api/v1/workflows/{id}/clone` | Clone workflow |

## 🔧 Troubleshooting

**Build fails:**
```bash
mvn clean install -U
```

**Database connection error:**
```bash
psql -U flowboard_user -d flowboard  # Test connection
```

**Table not created:**
- Check logs for Flyway migration messages
- Verify Flyway is enabled in `application.yml`

**Port 8080 already in use:**
```bash
lsof -ti :8080 | xargs kill -9  # Kill process on port 8080
# Or change port in application.yml
```
