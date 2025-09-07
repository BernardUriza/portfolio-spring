-- Reset all starred projects to UNSYNCED status
UPDATE starred_project SET sync_status = 'UNSYNCED', last_sync_attempt = NULL, sync_error_message = NULL;

-- Delete all generated Project, Skill, and Experience entities
DELETE FROM project;
DELETE FROM skill;
DELETE FROM experience;