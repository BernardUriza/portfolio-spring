# Repository Linkage Migration

This directory contains migration scripts to backfill repository linkage data for existing projects in the portfolio database.

## Overview

The migration performs the following operations:
1. **Repository Linking**: Links existing projects with GitHub repositories based on name patterns
2. **Field Protection**: Sets default field protection values (allows sync updates by default)
3. **Completion Status**: Assigns completion status based on existing project data
4. **Priority Assignment**: Sets project priorities based on characteristics
5. **Completion Percentage**: Calculates initial completion percentages using the standard algorithm

## Migration Options

### Option 1: SQL Script (Recommended for direct database access)

**File**: `migrate-repo-links.sql`

**Prerequisites**:
- Database backup
- Replace `:github_username` placeholder with your actual GitHub username

**Usage**:
```bash
# Connect to your database (example for H2)
java -cp h2*.jar org.h2.tools.Shell -url "jdbc:h2:mem:testdb" -user sa

# Run the script
\i migrate-repo-links.sql

# Review results and then commit or rollback
COMMIT; -- or ROLLBACK;
```

**Features**:
- Transaction-based (can rollback if issues occur)
- Detailed validation queries
- Migration statistics
- Manual override sections for specific projects

### Option 2: Java Migration Component

**File**: `src/main/java/com/portfolio/migration/RepoLinkageMigration.java`

**Usage**:
```bash
# Run with migration profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=migration

# Or add to application-migration.properties:
# spring.profiles.active=migration
```

**Features**:
- Integrated with Spring Boot
- Uses existing JPA repositories
- Comprehensive logging
- Automatic rollback on errors

## Migration Logic

### Repository URL Generation
Projects are linked to GitHub repositories using this pattern:
```
https://github.com/{username}/{project-name-sanitized}
```

Where `project-name-sanitized` is the project name:
- Converted to lowercase
- Special characters removed
- Spaces replaced with hyphens
- Multiple hyphens collapsed to single hyphens

### Completion Status Assignment
- **LIVE**: Projects with valid `live_demo_url`
- **IN_PROGRESS**: Projects with substantial description (50+ characters)
- **BACKLOG**: All other projects

### Priority Assignment
- **HIGH**: Projects with live demos
- **MEDIUM**: Projects with detailed descriptions (100+ characters)
- **LOW**: All other projects

### Completion Percentage Calculation
Using the standard algorithm:
- **Description** (40%): Has substantial description (50+ chars)
- **Live Demo** (20%): Has valid live demo URL
- **Skills** (20%): Assumed true for existing projects
- **Experiences** (20%): Based on GitHub repository presence

## Pre-Migration Checklist

- [ ] **Backup database**: Create a full backup before running migration
- [ ] **Set GitHub username**: Replace `:github_username` in SQL or set `github.username` in properties
- [ ] **Review project names**: Ensure project names will generate reasonable repository names
- [ ] **Test environment**: Run migration on development/staging first
- [ ] **Stop application**: Ensure no concurrent modifications during migration

## Post-Migration Validation

### Check Migration Results
```sql
-- Verify all projects have required fields
SELECT 
    COUNT(*) as total_projects,
    COUNT(CASE WHEN completion_status IS NOT NULL THEN 1 END) as with_status,
    COUNT(CASE WHEN priority IS NOT NULL THEN 1 END) as with_priority,
    COUNT(CASE WHEN completion_percentage > 0 THEN 1 END) as with_completion,
    COUNT(CASE WHEN github_url IS NOT NULL THEN 1 END) as with_github
FROM project;

-- Show distribution of completion status
SELECT completion_status, COUNT(*) as count 
FROM project 
GROUP BY completion_status;

-- Show projects needing manual review
SELECT name, github_url, completion_status, completion_percentage
FROM project 
WHERE github_url IS NULL OR completion_percentage = 0
ORDER BY name;
```

### Manual Corrections
After migration, you may need to manually correct:
1. **Incorrect GitHub URLs**: Fix repository links that don't match actual repositories
2. **Field Protection**: Enable protection for fields that shouldn't be overwritten by sync
3. **Completion Status**: Adjust status for projects where the algorithm guessed wrong
4. **Priority**: Reorder priorities based on actual project importance

## Example Manual Corrections

```sql
-- Fix specific GitHub URLs
UPDATE project 
SET github_url = 'https://github.com/your-username/actual-repo-name'
WHERE name = 'Project Display Name';

-- Enable field protection for important projects
UPDATE project 
SET description_protected = true, live_demo_url_protected = true
WHERE name IN ('Portfolio Frontend', 'Main Website');

-- Adjust completion status
UPDATE project 
SET completion_status = 'LIVE'
WHERE name = 'Production Project' AND live_demo_url IS NOT NULL;
```

## Rollback Instructions

### For SQL Migration
```sql
-- If you haven't committed yet
ROLLBACK;

-- If you need to restore from backup
-- 1. Stop the application
-- 2. Restore database from backup
-- 3. Restart application
```

### For Java Migration
The Java migration runs in a transaction, so errors will automatically rollback. If you need to manually rollback after completion, restore from your database backup.

## Troubleshooting

### Common Issues

1. **GitHub username not set**: Set `github.username` in application.properties or replace `:github_username` in SQL
2. **Permission errors**: Ensure database user has UPDATE permissions on project table
3. **Constraint violations**: Check for foreign key constraints that might prevent updates
4. **Memory issues**: For large datasets, consider processing projects in batches

### Validation Failures

If validation queries show unexpected results:
1. Review the migration logic for your specific use case
2. Adjust the completion status/priority assignment rules
3. Manually correct outliers after migration
4. Consider running the migration again with modified logic

## Support

For issues with the migration:
1. Check application logs for detailed error messages
2. Verify database schema matches expectations
3. Test with a small subset of projects first
4. Create GitHub issue with specific error details

---

**Important**: This migration is designed to be idempotent - it can be run multiple times safely. However, always backup your database first and test on a non-production environment.