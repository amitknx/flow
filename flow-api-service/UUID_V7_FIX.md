# UUID v7 Implementation - Hibernate 6 Compatibility Fix

## Problem

The original UUID v7 generator implementation was using Hibernate 5.x API (`IdentifierGenerator`), which is incompatible with Hibernate 6.x (used in Spring Boot 3.x).

**Error:**
```
Incompatible types.
Found: 'java.lang.Class<util.com.flow.flowboard.common.UuidV7Generator>'
Required: 'java.lang.Class<? extends org.hibernate.generator.Generator>'
```

## Solution

Updated the UUID v7 generator to use the new Hibernate 6.x `BeforeExecutionGenerator` interface.

---

## Changed Files

### 1. `flow-common/.../UuidV7Generator.java`

**Before (Hibernate 5):**
```java
import org.hibernate.id.IdentifierGenerator;

public class UuidV7Generator implements IdentifierGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
```

**After (Hibernate 6):**
```java
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

public class UuidV7Generator implements BeforeExecutionGenerator {

    @Override
    public Object generate(
        SharedSessionContractImplementor session,
        Object owner,
        Object currentValue,
        EventType eventType
    ) {
        return UuidCreator.getTimeOrderedEpoch();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}
```

### 2. `flow-api-service/.../domain/Workflow.java`

**Before:**
```java
import org.hibernate.annotations.GenericGenerator;

@Id
@GeneratedValue(generator = "uuid-v7")
@GenericGenerator(name = "uuid-v7", type = UuidV7Generator.class)
private UUID id;
```

**After (Hibernate 6 simplified syntax):**
```java
import org.hibernate.annotations.IdGeneratorType;

@Id
@IdGeneratorType(UuidV7Generator.class)
private UUID id;
```

---

## Key Changes

### BeforeExecutionGenerator Interface

The new Hibernate 6 API requires:

1. **Updated method signature**: Now accepts 4 parameters instead of 2
   - `owner` - The entity instance
   - `currentValue` - Current ID value (usually null)
   - `eventType` - INSERT, UPDATE, etc.

2. **Event type specification**: Must implement `getEventTypes()`
   - Returns `EventType.INSERT` to generate ID only on new entities

3. **Cleaner entity annotation**: Use `@IdGeneratorType` instead of `@GenericGenerator`

### Benefits

✅ **Hibernate 6 compatible** - Works with Spring Boot 3.x
✅ **Type-safe** - Better compile-time checking
✅ **Simplified** - Less verbose annotation syntax
✅ **Standards-compliant** - Follows latest Hibernate conventions

---

## Build & Test

```bash
# Clean and build
cd /Users/amit/dev/flow
mvn clean install

# Run the service
mvn spring-boot:run -pl flow-api-service

# Test UUID generation
curl -X POST http://localhost:8080/api/v1/workflows \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Workflow",
    "description": "Testing UUID v7",
    "data": {"nodes": [], "edges": []}
  }'

# Response should have time-ordered UUID v7
{
  "id": "01910d3a-5b42-7890-...",  # <-- UUID v7 format
  "name": "Test Workflow",
  ...
}
```

---

## Verification

### Check UUID v7 Format

UUID v7 has this structure:
```
01910d3a-5b42-7890-a123-456789abcdef
└─────┬─────┘ └┬┘
  timestamp    v7 (version 7)
```

- First 12 characters (48 bits): millisecond timestamp
- Character 13: Always '7' (version identifier)
- Remaining: Random bits

### Test Sequential Ordering

```sql
-- Create multiple workflows quickly
INSERT INTO workflows (id, name, data, ...) VALUES
  (gen_random_uuid(), 'Test 1', '{}', ...),
  (gen_random_uuid(), 'Test 2', '{}', ...),
  (gen_random_uuid(), 'Test 3', '{}', ...);

-- Check if UUIDs are time-ordered
SELECT id, name, created_at
FROM workflows
ORDER BY id;

-- UUIDs should be in chronological order!
```

---

## Compatibility Matrix

| Spring Boot | Hibernate | Generator Interface |
|-------------|-----------|---------------------|
| 2.x | 5.x | `IdentifierGenerator` |
| 3.x | 6.x | `BeforeExecutionGenerator` |

**Current Setup:** Spring Boot 3.3.3 → Hibernate 6.x → `BeforeExecutionGenerator` ✅

---

## Troubleshooting

### If you still get compilation errors:

1. **Clean Maven cache:**
   ```bash
   mvn clean
   rm -rf ~/.m2/repository/org/hibernate
   mvn install
   ```

2. **Verify Hibernate version:**
   ```bash
   mvn dependency:tree | grep hibernate
   # Should show hibernate-core 6.x
   ```

3. **Check imports:**
   ```java
   // Correct (Hibernate 6)
   import org.hibernate.generator.BeforeExecutionGenerator;

   // Incorrect (Hibernate 5)
   import org.hibernate.id.IdentifierGenerator;
   ```

---

## Alternative: Use @UuidGenerator (Built-in)

If you prefer using Hibernate's built-in UUID generator (though not v7):

```java
@Id
@UuidGenerator
private UUID id;  // Uses UUID v4 (random)
```

But our custom `UuidV7Generator` is better because:
- ✅ Time-ordered (better for indexes)
- ✅ Sequential (less fragmentation)
- ✅ Same uniqueness guarantees

---

## References

- [Hibernate 6 Migration Guide](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc)
- [UUID v7 RFC Draft](https://datatracker.ietf.org/doc/html/draft-peabody-dispatch-new-uuid-format)
- [uuid-creator Library](https://github.com/f4b6a3/uuid-creator)

---

The UUID v7 implementation is now fully compatible with Hibernate 6 and Spring Boot 3.x!
