# Quick Start: Database Migration for Audit Fields

## ⚠️ IMPORTANT: Run This Before Starting the Application

The application will fail with `Unknown column 'created_time'` error if you don't run this migration first.

---

## Step 1: Connect to Your MySQL Database

```bash
mysql -u postgres -p falconflow
# Enter password: Sa@1234
```

Or use your preferred MySQL client (MySQL Workbench, DBeaver, etc.)

---

## Step 2: Run the Migration Script

Copy and paste the following SQL commands:

```sql
-- ============================================
-- Add audit fields to ff_workspaces table
-- ============================================
ALTER TABLE ff_workspaces
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

UPDATE ff_workspaces SET created_by = 'system', modified_by = 'system' WHERE created_by IS NULL;

-- ============================================
-- Add audit fields to ff_projects table
-- ============================================
ALTER TABLE ff_projects
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

UPDATE ff_projects SET created_by = 'system', modified_by = 'system' WHERE created_by IS NULL;

-- ============================================
-- Add audit fields to ff_roles table
-- ============================================
ALTER TABLE ff_roles
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_time DATETIME;

UPDATE ff_roles SET created_by = 'system', modified_by = 'system' WHERE created_by IS NULL;

-- ============================================
-- Add audit fields to users table
-- ============================================
ALTER TABLE users
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

UPDATE users SET created_by = 'system', modified_by = 'system' WHERE created_by IS NULL;
```

---

## Step 3: Verify the Columns Were Added

```sql
-- Check ff_workspaces
DESCRIBE ff_workspaces;

-- Check ff_projects
DESCRIBE ff_projects;

-- Check ff_roles
DESCRIBE ff_roles;

-- Check users
DESCRIBE users;
```

You should see `created_by` and `modified_by` columns in all tables.

---

## Step 4: Start the Application

```bash
cd /Users/pratiknaik/Documents/works/falcon-flow-git/worker
./gradlew bootRun
```

The application should now start without errors!

---

## Alternative: One-Line Script

If you prefer to run it from the command line:

```bash
mysql -u postgres -p falconflow < /Users/pratiknaik/Documents/works/falcon-flow-git/worker/src/main/resources/db/migration/V1__add_audit_fields.sql
```

---

## Troubleshooting

### Issue: "Table doesn't exist"

**Solution:** The table name might be different. Check your actual table names:

```sql
SHOW TABLES;
```

### Issue: "Column already exists"

**Solution:** That's fine! The `IF NOT EXISTS` clause will skip it. Just continue.

### Issue: Still getting "Unknown column" error

**Solution:**

1. Stop the application
2. Verify columns exist: `DESCRIBE users;`
3. Restart the application

---

## What This Migration Does

1. **Adds `created_by` column** - Stores who created the record
2. **Adds `modified_by` column** - Stores who last modified the record
3. **Sets default values** - Existing records get "system" as the user
4. **Adds `modified_time` to roles** - For consistency with other entities

---

## After Migration

Once the migration is complete:

- ✅ All new records will automatically have audit fields populated
- ✅ Existing records will show "system" as the creator/modifier
- ✅ The application will start without errors
- ✅ You can view audit information via the API

---

## Testing After Migration

```bash
# Create a new workspace
curl -X POST http://localhost:4040/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -H "X-User-Id: john.doe" \
  -H "X-Username: john.doe" \
  -d '{
    "name": "Test Workspace",
    "code": "WS_TEST",
    "type": "DEVELOPMENT",
    "orgId": "ORG001"
  }'

# Verify audit fields are populated
curl http://localhost:4040/api/v1/workspaces
# Should show: "createdBy": "john.doe", "modifiedBy": "john.doe"
```

---

## Need Help?

If you encounter any issues:

1. Check the application logs
2. Verify database connection settings in `application-dev.properties`
3. Ensure MySQL is running
4. Check that you have the correct database permissions

---

## Summary

✅ **Before starting the app:** Run the migration SQL
✅ **After migration:** Start the application normally
✅ **Result:** Audit trail working automatically!
