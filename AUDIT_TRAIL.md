# Audit Trail Implementation

This document describes the audit trail feature that automatically tracks who created and modified entities in the FalconFlow system.

## Overview

The audit trail feature automatically populates the following fields for tracked entities:

- `createdBy`: Username/ID of the user who created the entity
- `modifiedBy`: Username/ID of the user who last modified the entity
- `createdTime`: Timestamp when the entity was created
- `modifiedTime`: Timestamp when the entity was last modified

## Tracked Entities

The following entities now include audit trail tracking:

1. **WorkSpaceEntity** (`ff_workspaces` table)
2. **ProjectEntity** (`ff_projects` table)
3. **RoleEntity** (`ff_roles` table)
4. **UserEntity** (`users` table)

## Architecture

### Backend Components

#### 1. AuditableEntity (Base Class)

- Location: `io.falconFlow.entity.AuditableEntity`
- Purpose: Base class that all auditable entities extend
- Fields:
  - `createdTime`: LocalDateTime (auto-populated on creation)
  - `modifiedTime`: LocalDateTime (auto-populated on update)
  - `createdBy`: String (auto-populated on creation)
  - `modifiedBy`: String (auto-populated on update)

#### 2. AuditEntityListener

- Location: `io.falconFlow.entity.AuditEntityListener`
- Purpose: JPA EntityListener that automatically populates audit fields
- Methods:
  - `@PrePersist setCreatedBy()`: Sets createdBy and modifiedBy on entity creation
  - `@PreUpdate setModifiedBy()`: Updates modifiedBy on entity modification

#### 3. UserContextHolder

- Location: `io.falconFlow.util.UserContextHolder`
- Purpose: ThreadLocal storage for current user context
- Methods:
  - `setCurrentUser(String username)`: Set the current user
  - `getCurrentUser()`: Get the current user (returns "system" if not set)
  - `clear()`: Clear the current user context

#### 4. UserContextInterceptor

- Location: `io.falconFlow.interceptor.UserContextInterceptor`
- Purpose: HTTP interceptor that extracts user info from request headers
- Headers checked (in order):
  1. `X-User-Id`: Preferred header for user identification
  2. `X-Username`: Alternative header for username
  3. Falls back to "system" if no headers present

## Usage

### Automatic Tracking

The audit trail is **completely automatic**. No code changes are required in services or controllers. When you save or update an entity that extends `AuditableEntity`, the audit fields are automatically populated.

```java
// Example: Creating a new project
ProjectEntity project = new ProjectEntity();
project.setName("My Project");
project.setCode("PROJ001");
projectRepository.save(project);
// createdBy and modifiedBy are automatically set based on current user context
```

### Setting User Context from Frontend

The frontend should include the user information in request headers:

```typescript
// Example: Angular HTTP Interceptor
const headers = {
  "X-User-Id": currentUser.userId,
  "X-Username": currentUser.username,
};
```

### Manual User Context (for Background Jobs)

For background jobs or system operations, you can manually set the user context:

```java
import io.falconFlow.util.UserContextHolder;

try {
    UserContextHolder.setCurrentUser("system-job");
    // Perform operations
    projectRepository.save(project);
} finally {
    UserContextHolder.clear(); // Always clear in finally block
}
```

## Database Schema

### New Columns Added

All tracked tables now have these additional columns:

```sql
created_by VARCHAR(100)    -- Username or user ID who created the record
modified_by VARCHAR(100)   -- Username or user ID who last modified the record
```

### Migration

The database schema is automatically updated by Hibernate (`ddl-auto=update`).

For manual migration, see: `src/main/resources/db/migration/V1__add_audit_fields.sql`

## Frontend Integration

### TypeScript Interfaces

All frontend models have been updated to include audit fields:

```typescript
export interface Workspace {
  // ... other fields
  createdTime?: number[];
  modifiedTime?: number[] | null;
  createdBy?: string;
  modifiedBy?: string;
}
```

### Displaying Audit Information

```typescript
// Example: Displaying who created a workspace
<div *ngIf="workspace.createdBy">
  Created by: {{ workspace.createdBy }}
  on {{ formatDateTime(workspace.createdTime) }}
</div>
```

## Configuration

### Interceptor Configuration

The `UserContextInterceptor` is registered in `CorsConfig.java`:

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
  registry.addInterceptor(userContextInterceptor)
      .addPathPatterns("/api/**"); // Apply to all API endpoints
}
```

### Customization

To customize the user identification logic, modify:

- `UserContextInterceptor.preHandle()`: Change how user is extracted from request
- `AuditEntityListener`: Change how audit fields are populated

## Best Practices

1. **Always send user headers**: Frontend should always include `X-User-Id` or `X-Username` headers
2. **Use meaningful identifiers**: Use actual usernames or user IDs, not generic values
3. **Clear context in background jobs**: Always use try-finally to clear user context
4. **Don't manually set audit fields**: Let the system handle it automatically

## Testing

### Unit Tests

```java
@Test
public void testAuditFieldsPopulated() {
    UserContextHolder.setCurrentUser("test-user");

    ProjectEntity project = new ProjectEntity();
    project.setName("Test Project");
    projectRepository.save(project);

    assertEquals("test-user", project.getCreatedBy());
    assertEquals("test-user", project.getModifiedBy());
    assertNotNull(project.getCreatedTime());

    UserContextHolder.clear();
}
```

### Integration Tests

Test with actual HTTP requests including headers:

```bash
curl -X POST http://localhost:4040/api/v1/projects \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{"name":"Test Project","code":"TEST001"}'
```

## Troubleshooting

### Audit fields are null

- Check that request includes `X-User-Id` or `X-Username` header
- Verify `UserContextInterceptor` is registered
- Check that entity extends `AuditableEntity`

### Wrong user in audit fields

- Verify correct header values are being sent
- Check interceptor is extracting user correctly
- Ensure user context is cleared between requests

### Background jobs show "system"

- This is expected behavior when no user context is set
- Manually set user context if you want a specific user

## Future Enhancements

1. **JWT Integration**: Extract user from JWT token instead of headers
2. **Audit History Table**: Store complete change history, not just last modified
3. **Field-level Auditing**: Track which specific fields changed
4. **Soft Deletes**: Add `deletedBy` and `deletedTime` fields
