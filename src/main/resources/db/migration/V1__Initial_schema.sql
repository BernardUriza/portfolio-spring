-- Flyway Migration V1: Initial Database Schema
-- Created by Bernard Orozco
-- Description: Baseline schema for Portfolio Backend

-- =============================================================================
-- Table: skills
-- Description: Stores user skills and competencies
-- =============================================================================
CREATE TABLE skills (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    level VARCHAR(50) NOT NULL,
    years_of_experience INTEGER,
    is_featured BOOLEAN DEFAULT FALSE,
    icon_url VARCHAR(255),
    documentation_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_skill_category ON skills(category);
CREATE INDEX idx_skill_level ON skills(level);
CREATE INDEX idx_skill_featured ON skills(is_featured);
CREATE INDEX idx_skill_experience ON skills(years_of_experience);
CREATE INDEX idx_skill_cat_level ON skills(category, level);
CREATE INDEX idx_skill_created_at ON skills(created_at);

-- =============================================================================
-- Table: experiences
-- Description: Stores work experience and employment history
-- =============================================================================
CREATE TABLE experiences (
    id BIGSERIAL PRIMARY KEY,
    job_title VARCHAR(200) NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    company_url VARCHAR(255),
    location VARCHAR(255),
    type VARCHAR(50) NOT NULL,
    description VARCHAR(2000),
    start_date DATE NOT NULL,
    end_date DATE,
    is_current_position BOOLEAN DEFAULT TRUE,
    company_logo_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_exp_type ON experiences(type);
CREATE INDEX idx_exp_current ON experiences(is_current_position);
CREATE INDEX idx_exp_company ON experiences(company_name);
CREATE INDEX idx_exp_start_date ON experiences(start_date);
CREATE INDEX idx_exp_end_date ON experiences(end_date);
CREATE INDEX idx_exp_type_start ON experiences(type, start_date);
CREATE INDEX idx_exp_current_start ON experiences(is_current_position, start_date);

-- Experience collection tables
CREATE TABLE experience_achievements (
    experience_id BIGINT NOT NULL,
    achievement VARCHAR(255),
    FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE
);

CREATE TABLE experience_technologies (
    experience_id BIGINT NOT NULL,
    technology VARCHAR(255),
    FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE
);

CREATE TABLE experience_skill_ids (
    experience_id BIGINT NOT NULL,
    skill_id BIGINT,
    FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE
);

-- =============================================================================
-- Table: source_repositories
-- Description: Stores GitHub repository metadata
-- =============================================================================
CREATE TABLE source_repositories (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    full_name VARCHAR(300) NOT NULL,
    description VARCHAR(1000),
    github_repo_url VARCHAR(500) NOT NULL UNIQUE,
    homepage VARCHAR(500),
    language VARCHAR(50),
    is_fork BOOLEAN,
    stargazers_count INTEGER,
    github_created_at VARCHAR(255),
    github_updated_at VARCHAR(255),
    readme_markdown TEXT,
    sync_status VARCHAR(50) DEFAULT 'UNSYNCED',
    last_sync_attempt TIMESTAMP,
    sync_error_message VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_source_github_id ON source_repositories(github_id);
CREATE INDEX idx_source_full_name ON source_repositories(full_name);
CREATE INDEX idx_source_sync_status ON source_repositories(sync_status);
CREATE INDEX idx_source_language ON source_repositories(language);
CREATE INDEX idx_source_updated_at ON source_repositories(updated_at);
CREATE INDEX idx_source_sync_updated ON source_repositories(sync_status, updated_at);
CREATE INDEX idx_source_lang_sync ON source_repositories(language, sync_status);
CREATE INDEX idx_source_stars ON source_repositories(stargazers_count);

-- Source repository collection table
CREATE TABLE source_repository_topics (
    source_repository_id BIGINT NOT NULL,
    topic VARCHAR(255),
    FOREIGN KEY (source_repository_id) REFERENCES source_repositories(id) ON DELETE CASCADE
);

-- =============================================================================
-- Table: portfolio_projects
-- Description: Main portfolio projects table
-- =============================================================================
CREATE TABLE portfolio_projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    link VARCHAR(255),
    github_repo VARCHAR(255),
    created_date DATE NOT NULL,
    estimated_duration_weeks INTEGER,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    type VARCHAR(50) DEFAULT 'PERSONAL',
    source_repository_id BIGINT,
    link_type VARCHAR(50),
    repository_id BIGINT,
    repository_full_name VARCHAR(300),
    repository_url VARCHAR(500),
    repository_stars INTEGER,
    default_branch VARCHAR(100),
    completion_status VARCHAR(50) DEFAULT 'BACKLOG',
    priority VARCHAR(50),
    protect_description BOOLEAN DEFAULT FALSE,
    protect_live_demo_url BOOLEAN DEFAULT FALSE,
    protect_skills BOOLEAN DEFAULT FALSE,
    protect_experiences BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT,
    manual_description_override BOOLEAN DEFAULT FALSE,
    manual_link_override BOOLEAN DEFAULT FALSE,
    manual_skills_override BOOLEAN DEFAULT FALSE,
    manual_experiences_override BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (source_repository_id) REFERENCES source_repositories(id) ON DELETE SET NULL
);

CREATE INDEX idx_portfolio_status ON portfolio_projects(status);
CREATE INDEX idx_portfolio_completion_status ON portfolio_projects(completion_status);
CREATE INDEX idx_portfolio_source_repo ON portfolio_projects(source_repository_id);
CREATE INDEX idx_portfolio_updated_at ON portfolio_projects(updated_at);
CREATE INDEX idx_portfolio_created_date ON portfolio_projects(created_date);
CREATE INDEX idx_portfolio_type ON portfolio_projects(type);
CREATE INDEX idx_portfolio_status_updated ON portfolio_projects(status, updated_at);

-- Portfolio project collection tables
CREATE TABLE portfolio_project_technologies (
    portfolio_project_id BIGINT NOT NULL,
    technology VARCHAR(255),
    FOREIGN KEY (portfolio_project_id) REFERENCES portfolio_projects(id) ON DELETE CASCADE
);

CREATE TABLE portfolio_project_skill_ids (
    portfolio_project_id BIGINT NOT NULL,
    skill_id BIGINT,
    FOREIGN KEY (portfolio_project_id) REFERENCES portfolio_projects(id) ON DELETE CASCADE
);

CREATE TABLE portfolio_project_experience_ids (
    portfolio_project_id BIGINT NOT NULL,
    experience_id BIGINT,
    FOREIGN KEY (portfolio_project_id) REFERENCES portfolio_projects(id) ON DELETE CASCADE
);

-- =============================================================================
-- Table: sync_config
-- Description: Singleton configuration for sync scheduling
-- =============================================================================
CREATE TABLE sync_config (
    id BIGSERIAL PRIMARY KEY,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    interval_hours INTEGER NOT NULL DEFAULT 6 CHECK (interval_hours >= 1 AND interval_hours <= 168),
    last_run_at TIMESTAMP,
    next_run_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255) NOT NULL DEFAULT 'admin',
    singleton_key VARCHAR(1) NOT NULL DEFAULT 'X',
    CONSTRAINT uk_sync_config_singleton UNIQUE (singleton_key)
);

-- Insert default sync config (singleton)
INSERT INTO sync_config (enabled, interval_hours, singleton_key)
VALUES (FALSE, 6, 'X');

-- =============================================================================
-- Table: reset_audit
-- Description: Audit log for factory reset operations
-- =============================================================================
CREATE TABLE reset_audit (
    id BIGSERIAL PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL UNIQUE,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    started_by VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(1000),
    tables_cleared INTEGER,
    duration_ms BIGINT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reset_job_id ON reset_audit(job_id);
CREATE INDEX idx_reset_status ON reset_audit(status);
CREATE INDEX idx_reset_started_at ON reset_audit(started_at);
CREATE INDEX idx_reset_status_started ON reset_audit(status, started_at);

-- =============================================================================
-- Table: contact_messages
-- Description: Contact form submissions
-- =============================================================================
CREATE TABLE contact_messages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    company VARCHAR(150),
    subject VARCHAR(150) NOT NULL,
    message VARCHAR(4000) NOT NULL,
    source_path VARCHAR(200),
    session_id VARCHAR(64),
    user_agent VARCHAR(400),
    ip_hash VARCHAR(64),
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    honeypot VARCHAR(255)
);

-- Contact message collection table
CREATE TABLE contact_message_labels (
    message_id BIGINT NOT NULL,
    label VARCHAR(255),
    FOREIGN KEY (message_id) REFERENCES contact_messages(id) ON DELETE CASCADE
);

-- =============================================================================
-- Table: visitor_insights
-- Description: Visitor session tracking and insights
-- =============================================================================
CREATE TABLE visitor_insights (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL UNIQUE,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    duration_seconds INTEGER,
    pages_visited INTEGER DEFAULT 0,
    actions TEXT,
    ai_conclusion VARCHAR(4000),
    contact_message_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Visitor insight collection table
CREATE TABLE visitor_insight_projects (
    insight_id BIGINT NOT NULL,
    project_repo VARCHAR(255),
    FOREIGN KEY (insight_id) REFERENCES visitor_insights(id) ON DELETE CASCADE
);

-- =============================================================================
-- Comments for documentation
-- =============================================================================
COMMENT ON TABLE skills IS 'User skills and competencies';
COMMENT ON TABLE experiences IS 'Work experience and employment history';
COMMENT ON TABLE source_repositories IS 'GitHub repository metadata from starred repos';
COMMENT ON TABLE portfolio_projects IS 'Main portfolio projects derived from repositories';
COMMENT ON TABLE sync_config IS 'Singleton configuration for GitHub sync scheduling';
COMMENT ON TABLE reset_audit IS 'Audit log for factory reset operations';
COMMENT ON TABLE contact_messages IS 'Contact form submissions from visitors';
COMMENT ON TABLE visitor_insights IS 'Visitor session tracking and analytics';
