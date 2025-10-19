# Database Setup Guide

## PostgreSQL Setup for Flowboard

### 1. Install PostgreSQL

**macOS (Homebrew):**
```bash
brew install postgresql@16
brew services start postgresql@16
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Docker:**
```bash
docker run --name flowboard-postgres \
  -e POSTGRES_DB=flowboard \
  -e POSTGRES_USER=flowboard_user \
  -e POSTGRES_PASSWORD=flowboard_pass \
  -p 5432:5432 \
  -d postgres:16-alpine
```

---

### 2. Create Database and User

**Connect to PostgreSQL:**
```bash
psql -U postgres
```

**Run Setup Commands:**
```sql
-- Create user
CREATE USER flowboard_user WITH PASSWORD 'flowboard_pass';

-- Create database
CREATE DATABASE flowboard OWNER flowboard_user;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE flowboard TO flowboard_user;

-- Connect to flowboard database
\c flowboard

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO flowboard_user;

-- Exit psql
\q
```

---

### 3. Option A: Automatic Migration (Recommended)

The application uses **Flyway** for automatic database migrations.

**Start the application:**
```bash
cd /Users/amit/dev/flow
mvn spring-boot:run -pl flow-api-service
```

Flyway will automatically:
- Create the `workflows` table
- Create all indexes
- Add comments to schema

**Check migration status:**
```bash
# In application logs you'll see:
# Flyway: Successfully validated 1 migration
# Flyway: Successfully applied 1 migration to schema "public"
```

---

### 4. Option B: Manual Table Creation

If you prefer manual setup or need to run SQL directly:

**Run the SQL file:**
```bash
psql -U flowboard_user -d flowboard -f create-workflows-table.sql
```

**Or copy/paste SQL:**
```sql
-- Create workflows table
CREATE TABLE workflows (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    data JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    modified_at TIMESTAMP NOT NULL,
    modified_by VARCHAR(100)
);

-- Indexes
CREATE INDEX idx_workflows_active ON workflows(active);
CREATE INDEX idx_workflows_search ON workflows USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '')));
CREATE INDEX idx_workflows_created_at ON workflows(created_at DESC);
CREATE INDEX idx_workflows_data ON workflows USING gin(data);
CREATE INDEX idx_workflows_active_created ON workflows(active, created_at DESC);
```

---

### 5. Verify Table Creation

**Connect to database:**
```bash
psql -U flowboard_user -d flowboard
```

**Check table:**
```sql
-- List tables
\dt

-- Describe workflows table
\d workflows

-- Check indexes
\di

-- Expected output:
--  Schema |       Name       | Type  |     Owner
-- --------+------------------+-------+----------------
--  public | workflows        | table | flowboard_user
```

---

### 6. Test Data Insertion

**Insert sample workflow:**
```sql
INSERT INTO workflows (
    id,
    name,
    description,
    data,
    active,
    created_at,
    created_by,
    modified_at,
    modified_by
) VALUES (
    gen_random_uuid(),
    'Test Workflow',
    'Sample workflow for testing',
    '{"nodes": [{"id": "start", "type": "START", "position": {"x": 100, "y": 100}}], "edges": []}',
    true,
    NOW(),
    'system',
    NOW(),
    'system'
);

-- Query to verify
SELECT id, name, active, created_at FROM workflows;
```

---

### 7. Configuration

**Update `application.yml` if needed:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/flowboard
    username: flowboard_user
    password: flowboard_pass

  flyway:
    enabled: true  # Set to false to disable auto-migration
```

**Environment variables (alternative):**
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/flowboard
export SPRING_DATASOURCE_USERNAME=flowboard_user
export SPRING_DATASOURCE_PASSWORD=flowboard_pass
```

---

### 8. Common Issues

**Issue: "role does not exist"**
```sql
CREATE USER flowboard_user WITH PASSWORD 'flowboard_pass';
```

**Issue: "database does not exist"**
```sql
CREATE DATABASE flowboard;
```

**Issue: "permission denied for schema public"**
```sql
\c flowboard
GRANT ALL ON SCHEMA public TO flowboard_user;
```

**Issue: Connection refused**
```bash
# Check if PostgreSQL is running
brew services list  # macOS
sudo systemctl status postgresql  # Linux

# Check port
lsof -i :5432
```

---

### 9. Database Management Commands

**Drop and recreate (CAUTION - deletes all data):**
```sql
DROP DATABASE IF EXISTS flowboard;
CREATE DATABASE flowboard OWNER flowboard_user;
```

**Reset Flyway migrations:**
```sql
\c flowboard
DELETE FROM flyway_schema_history;
```

**Backup database:**
```bash
pg_dump -U flowboard_user flowboard > flowboard_backup.sql
```

**Restore database:**
```bash
psql -U flowboard_user flowboard < flowboard_backup.sql
```

---

### 10. Performance Tuning (Optional)

**For development:**
```sql
-- Increase shared buffers (in postgresql.conf)
shared_buffers = 256MB

-- Increase work memory
work_mem = 16MB

-- Enable query statistics
shared_preload_libraries = 'pg_stat_statements'
```

---

## Table Schema Reference

```sql
Column      | Type         | Nullable | Description
------------|--------------|----------|------------------------------------
id          | UUID         | NOT NULL | UUID v7 primary key (time-ordered)
name        | VARCHAR(200) | NOT NULL | Workflow name
description | VARCHAR(2000)| NULL     | Optional description
data        | JSONB        | NOT NULL | React Flow JSON data
active      | BOOLEAN      | NOT NULL | Soft delete flag (default: true)
created_at  | TIMESTAMP    | NOT NULL | Creation timestamp
created_by  | VARCHAR(100) | NULL     | Creator username
modified_at | TIMESTAMP    | NOT NULL | Last modification timestamp
modified_by | VARCHAR(100) | NULL     | Last modifier username
```

---

## Next Steps

1. ✅ Create database and user
2. ✅ Run application (Flyway auto-migrates)
3. ✅ Verify table creation
4. ✅ Test API endpoints
5. 🔄 Monitor performance and adjust indexes as needed

For API testing, see the main README.md file.
