-- Roles (Long PK with IDENTITY, not UUID)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    user_role VARCHAR(50) NOT NULL UNIQUE
);

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name VARCHAR(100) NOT NULL,
    user_email VARCHAR(250) NOT NULL UNIQUE,
    user_password VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- User roles join table
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Projects
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_name VARCHAR(100) NOT NULL,
    project_description VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Project members join table (from User.projects ManyToMany)
CREATE TABLE project_members (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, project_id)
);

-- Tasks (note: taskCategory column name must be quoted to preserve case in PostgreSQL)
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_title VARCHAR(255) NOT NULL,
    task_description TEXT,
    task_status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    task_category VARCHAR(50) NOT NULL,
    task_priority VARCHAR(50) NOT NULL,
    task_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    task_project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at TIMESTAMP,
    last_updated_at TIMESTAMP,
    due_date DATE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_task_user ON tasks(task_user_id);
CREATE INDEX idx_task_project ON tasks(task_project_id);

-- Audit logs (audit_id is the PK column name, timeStamp must be quoted)
CREATE TABLE audit_logs (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50),
    entity_id UUID NOT NULL,
    action VARCHAR(50),
    field_name VARCHAR(100),
    old_property_value TEXT,
    new_property_value TEXT,
    performed_by UUID,
    time_stamp TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Password reset tokens (expiryTime → expiry_time via Spring naming strategy)
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) UNIQUE,
    expiry_time TIMESTAMP,
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Email verification tokens (expiryDate → expiry_date)
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP NOT NULL
);

-- Refresh tokens (expiryDate → expiry_date, createdAt → created_at)
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(36) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL
);