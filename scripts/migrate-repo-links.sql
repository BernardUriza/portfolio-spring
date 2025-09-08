-- Data Migration Script: Repository Linkage Backfill
-- Purpose: Link existing projects with their corresponding GitHub repositories
-- Author: Claude Code
-- Date: 2025-09-08

-- This script performs the following actions:
-- 1. Updates existing projects with GitHub repository URLs where matches can be found
-- 2. Sets default field protection values for all existing projects
-- 3. Calculates initial completion percentages based on existing data

-- IMPORTANT: Run this script after deploying the new Project entity structure
-- IMPORTANT: Backup your database before running this migration

-- Enable transaction to allow rollback if needed
BEGIN TRANSACTION;

-- Step 1: Add github_url for projects that can be matched by name similarity
-- This assumes you have a pattern in project names that matches repository names
UPDATE project p
SET github_url = CONCAT('https://github.com/', :github_username, '/', 
    LOWER(REPLACE(REPLACE(REPLACE(p.name, ' ', '-'), '.', ''), '_', '-')))
WHERE p.github_url IS NULL
  AND p.name IS NOT NULL
  AND LENGTH(TRIM(p.name)) > 0
  -- Only update if the generated URL pattern makes sense (contains valid characters)
  AND p.name NOT LIKE '%/%'
  AND p.name NOT LIKE '%@%'
  AND p.name NOT LIKE '%#%';

-- Step 2: Set default field protection for all existing projects
-- By default, no fields are protected (allow sync to update them)
UPDATE project 
SET description_protected = false,
    live_demo_url_protected = false
WHERE description_protected IS NULL 
   OR live_demo_url_protected IS NULL;

-- Step 3: Set default completion status for existing projects
-- Projects with live_demo_url are considered LIVE, others are IN_PROGRESS
UPDATE project 
SET completion_status = CASE 
    WHEN live_demo_url IS NOT NULL AND LENGTH(TRIM(live_demo_url)) > 0 THEN 'LIVE'
    WHEN description IS NOT NULL AND LENGTH(TRIM(description)) > 50 THEN 'IN_PROGRESS'
    ELSE 'BACKLOG'
END
WHERE completion_status IS NULL;

-- Step 4: Set default priority based on project characteristics
-- Projects with demos get HIGH priority, well-described ones get MEDIUM, others LOW
UPDATE project 
SET priority = CASE 
    WHEN live_demo_url IS NOT NULL AND LENGTH(TRIM(live_demo_url)) > 0 THEN 'HIGH'
    WHEN description IS NOT NULL AND LENGTH(TRIM(description)) > 100 THEN 'MEDIUM'
    ELSE 'LOW'
END
WHERE priority IS NULL;

-- Step 5: Calculate initial completion percentage based on existing data
-- This uses the same algorithm as the backend service:
-- Description: 40%, Live Demo: 20%, Skills: 20%, Experiences: 20%
UPDATE project 
SET completion_percentage = (
    -- Description component (40%)
    CASE WHEN description IS NOT NULL AND LENGTH(TRIM(description)) >= 50 THEN 40 ELSE 0 END +
    
    -- Live Demo component (20%)
    CASE WHEN live_demo_url IS NOT NULL AND LENGTH(TRIM(live_demo_url)) > 0 THEN 20 ELSE 0 END +
    
    -- Skills component (20%) - approximate based on project complexity
    CASE WHEN description IS NOT NULL AND LENGTH(TRIM(description)) > 100 THEN 20 ELSE 10 END +
    
    -- Experiences component (20%) - approximate based on github presence
    CASE WHEN github_url IS NOT NULL THEN 20 ELSE 0 END
)
WHERE completion_percentage IS NULL OR completion_percentage = 0;

-- Step 6: Update timestamps for audit trail
UPDATE project 
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;

-- Step 7: Manual fixes for known projects (customize this section for your specific projects)
-- Example: If you know specific GitHub URLs for certain projects

-- Uncomment and modify these examples for your actual projects:
/*
UPDATE project 
SET github_url = 'https://github.com/your-username/portfolio-frontend',
    description_protected = true,  -- Protect from sync overwrites
    priority = 'HIGH'
WHERE name LIKE '%Portfolio%' OR name LIKE '%portfolio%';

UPDATE project 
SET github_url = 'https://github.com/your-username/ecommerce-backend',
    live_demo_url_protected = true,
    completion_status = 'LIVE'
WHERE name LIKE '%E-commerce%' OR name LIKE '%ecommerce%';
*/

-- Step 8: Log migration results
-- Create a temporary table to store migration statistics
CREATE TEMPORARY TABLE migration_stats AS
SELECT 
    COUNT(*) as total_projects,
    COUNT(CASE WHEN github_url IS NOT NULL THEN 1 END) as projects_with_github,
    COUNT(CASE WHEN completion_status = 'LIVE' THEN 1 END) as live_projects,
    COUNT(CASE WHEN completion_status = 'IN_PROGRESS' THEN 1 END) as in_progress_projects,
    COUNT(CASE WHEN completion_status = 'BACKLOG' THEN 1 END) as backlog_projects,
    COUNT(CASE WHEN description_protected = true OR live_demo_url_protected = true THEN 1 END) as protected_projects,
    AVG(completion_percentage) as avg_completion
FROM project;

-- Display migration results
SELECT 
    'MIGRATION COMPLETE' as status,
    total_projects,
    projects_with_github,
    live_projects,
    in_progress_projects,
    backlog_projects,
    protected_projects,
    ROUND(avg_completion, 2) as avg_completion_percentage
FROM migration_stats;

-- Step 9: Validation queries to verify migration success
-- Uncomment these to run validation checks:

/*
-- Check for projects without completion status
SELECT 'Projects missing completion status:' as check_type, COUNT(*) as count
FROM project WHERE completion_status IS NULL;

-- Check for projects with invalid completion percentages
SELECT 'Projects with invalid completion percentage:' as check_type, COUNT(*) as count
FROM project WHERE completion_percentage < 0 OR completion_percentage > 100;

-- Check for projects missing field protection settings
SELECT 'Projects missing field protection:' as check_type, COUNT(*) as count
FROM project WHERE description_protected IS NULL OR live_demo_url_protected IS NULL;

-- Show sample of migrated projects
SELECT 
    name,
    completion_status,
    priority,
    completion_percentage,
    CASE WHEN github_url IS NOT NULL THEN 'YES' ELSE 'NO' END as has_github,
    CASE WHEN description_protected OR live_demo_url_protected THEN 'YES' ELSE 'NO' END as has_protection
FROM project
ORDER BY completion_percentage DESC
LIMIT 10;
*/

-- IMPORTANT: Review the results above before committing
-- If everything looks correct, run: COMMIT;
-- If there are issues, run: ROLLBACK;

-- COMMIT; -- Uncomment this line when you're satisfied with the migration results
-- ROLLBACK; -- Use this instead if you need to undo the changes

-- Clean up temporary table
DROP TABLE IF EXISTS migration_stats;