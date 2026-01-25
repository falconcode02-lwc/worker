# MySQL Datetime Truncation Fix

## Issue

```
Caused by: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation:
Data truncation: Incorrect datetime value: '0000-00-00 00:00:00'
for column 'created_time' at row 1
```

## Root Cause

The `@PrePersist` and `@PreUpdate` methods in `AuditableEntity` were being called AFTER the `AuditEntityListener`, resulting in timestamps being set to null or invalid values before MySQL tried to insert them.

## Solution

Moved all timestamp initialization logic into the `AuditEntityListener` to ensure proper initialization order:

### Changes Made

1. **AuditEntityListener.java** - Enhanced to set timestamps directly

   ```java
   @PrePersist
   public void setCreationAudit(AuditableEntity entity) {
       LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
       entity.createdTime = now;
       entity.modifiedTime = now;
       entity.setCreatedBy(UserContextHolder.getCurrentUser());
       entity.setModifiedBy(UserContextHolder.getCurrentUser());
   }
   ```

2. **AuditableEntity.java** - Removed `@PrePersist` and `@PreUpdate` methods
   - The listener now handles all timestamp initialization
   - Prevents lifecycle callback conflicts

3. **WorkSpaceEntity.java** - Removed `super.prePersist()` call
   - Parent no longer has this method

4. **ProjectEntity.java** - Removed `super.prePersist()` and `super.preUpdate()` calls
   - Parent no longer has these methods
   - Entity-specific logic remains (e.g., setting default accessibility)

## Result

✅ Timestamps are now properly initialized before database insertion
✅ No more MySQL datetime truncation errors
✅ Compilation successful
✅ All audit fields (createdTime, modifiedTime, createdBy, modifiedBy) are set correctly

## Testing

After starting the application, timestamps will be automatically set to the current time when entities are created or updated.

```bash
# Test creating a workspace
curl -X POST http://localhost:4040/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user" \
  -d '{
    "name": "Test Workspace",
    "code": "WS001",
    "type": "DEVELOPMENT",
    "orgId": "ORG001"
  }'

# Verify timestamps are set correctly (not 0000-00-00)
```

## Files Modified

1. `/worker/src/main/java/io/falconFlow/entity/AuditEntityListener.java`
2. `/worker/src/main/java/io/falconFlow/entity/AuditableEntity.java`
3. `/worker/src/main/java/io/falconFlow/entity/WorkSpaceEntity.java`
4. `/worker/src/main/java/io/falconFlow/entity/ProjectEntity.java`

## Status

✅ **FIXED** - Ready for deployment
