-- ============================================
-- COMPLETE MIGRATION SCRIPT FOR AUDIT FIELDS
-- Run this BEFORE starting the application
-- ============================================

-- ============================================
-- 1. ff_workspaces table
-- ============================================
ALTER TABLE ff_workspaces 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

UPDATE ff_workspaces 
SET created_by = 'system', modified_by = 'system' 
WHERE created_by IS NULL;

-- ============================================
-- 2. ff_projects table
-- ============================================
ALTER TABLE ff_projects 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

UPDATE ff_projects 
SET created_by = 'system', modified_by = 'system' 
WHERE created_by IS NULL;

-- ============================================
-- 3. ff_roles table
-- ============================================
ALTER TABLE ff_roles 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_time DATETIME;

-- Rename created_at to created_time (if created_at exists)
SET @dbname = DATABASE();
SET @tablename = 'ff_roles';
SET @columnexists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_SCHEMA = @dbname 
                     AND TABLE_NAME = @tablename 
                     AND COLUMN_NAME = 'created_at');

SET @query = IF(@columnexists > 0, 
    'ALTER TABLE ff_roles CHANGE COLUMN created_at created_time DATETIME', 
    'SELECT "Column created_at does not exist, skipping rename" AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE ff_roles 
SET created_by = 'system', modified_by = 'system' 
WHERE created_by IS NULL;

-- ============================================
-- 4. users table (CRITICAL - this is failing)
-- ============================================
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS modified_by VARCHAR(100);

-- Add created_time column if it doesn't exist
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnexists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_SCHEMA = @dbname 
                     AND TABLE_NAME = @tablename 
                     AND COLUMN_NAME = 'created_time');

SET @query = IF(@columnexists = 0, 
    'ALTER TABLE users ADD COLUMN created_time DATETIME', 
    'SELECT "Column created_time already exists" AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add modified_time column if it doesn't exist
SET @columnexists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_SCHEMA = @dbname 
                     AND TABLE_NAME = @tablename 
                     AND COLUMN_NAME = 'modified_time');

SET @query = IF(@columnexists = 0, 
    'ALTER TABLE users ADD COLUMN modified_time DATETIME', 
    'SELECT "Column modified_time already exists" AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Copy data from created_at to created_time if created_at exists
UPDATE users 
SET created_time = created_at 
WHERE created_at IS NOT NULL AND created_time IS NULL;

-- Copy data from updated_at to modified_time if updated_at exists
UPDATE users 
SET modified_time = updated_at 
WHERE updated_at IS NOT NULL AND modified_time IS NULL;

-- Set default values for audit user fields
UPDATE users 
SET created_by = 'system', modified_by = 'system' 
WHERE created_by IS NULL;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify the migration worked:
-- SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND TABLE_SCHEMA = DATABASE();
-- SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ff_roles' AND TABLE_SCHEMA = DATABASE();
-- SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ff_projects' AND TABLE_SCHEMA = DATABASE();
-- SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ff_workspaces' AND TABLE_SCHEMA = DATABASE();
