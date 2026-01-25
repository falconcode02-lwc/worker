# SIMPLE DATABASE MIGRATION - RUN THIS NOW

## Quick Fix for "Unknown column 'created_time'" Error

Run these SQL commands in your MySQL database:

```sql
-- Connect to database first:
-- mysql -u postgres -p falconflow

-- ============================================
-- Add missing columns to users table
-- ============================================
ALTER TABLE users
ADD COLUMN created_time DATETIME,
ADD COLUMN modified_time DATETIME,
ADD COLUMN created_by VARCHAR(100),
ADD COLUMN modified_by VARCHAR(100);

-- Copy existing timestamp data
UPDATE users SET created_time = created_at WHERE created_at IS NOT NULL;
UPDATE users SET modified_time = updated_at WHERE updated_at IS NOT NULL;
UPDATE users SET created_by = 'system', modified_by = 'system';

-- ============================================
-- Add missing columns to ff_roles table
-- ============================================
ALTER TABLE ff_roles
ADD COLUMN modified_time DATETIME,
ADD COLUMN created_by VARCHAR(100),
ADD COLUMN modified_by VARCHAR(100);

UPDATE ff_roles SET created_by = 'system', modified_by = 'system';

-- ============================================
-- Add missing columns to ff_projects table
-- ============================================
ALTER TABLE ff_projects
ADD COLUMN created_by VARCHAR(100),
ADD COLUMN modified_by VARCHAR(100);

UPDATE ff_projects SET created_by = 'system', modified_by = 'system';

-- ============================================
-- Add missing columns to ff_workspaces table
-- ============================================
ALTER TABLE ff_workspaces
ADD COLUMN created_by VARCHAR(100),
ADD COLUMN modified_by VARCHAR(100);

UPDATE ff_workspaces SET created_by = 'system', modified_by = 'system';
```

## Verify Migration

```sql
-- Check users table
DESCRIBE users;

-- You should see:
-- created_time, modified_time, created_by, modified_by
```

## If Columns Already Exist

If you get "Duplicate column name" error, that's OK! It means the column already exists. Just skip that ALTER TABLE command and continue with the next one.

## After Migration

Restart your application:

```bash
cd /Users/pratiknaik/Documents/works/falcon-flow-git/worker
./gradlew bootRun
```

The error should be gone! ✅
