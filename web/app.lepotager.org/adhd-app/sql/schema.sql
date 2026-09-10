CREATE TABLE IF NOT EXISTS survey_sessions (
    id CHAR(36) PRIMARY KEY,
    resume_token_hash CHAR(64) NOT NULL UNIQUE,
    status ENUM('active','completed','deleted') NOT NULL DEFAULT 'active',
    current_step VARCHAR(100) NOT NULL DEFAULT 'welcome',
    primary_path VARCHAR(50) NULL,
    secondary_path VARCHAR(50) NULL,
    locale VARCHAR(10) NOT NULL DEFAULT 'en',
    consent_at DATETIME NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_survey_status_updated (status, updated_at),
    INDEX idx_survey_completed_at (completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS survey_answers (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    question_id VARCHAR(100) NOT NULL,
    answer_json JSON NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_session_question (session_id, question_id),
    CONSTRAINT fk_answer_session FOREIGN KEY (session_id) REFERENCES survey_sessions(id) ON DELETE CASCADE,
    INDEX idx_answer_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resume_contacts (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    session_id CHAR(36) NOT NULL UNIQUE,
    email_encrypted TEXT NOT NULL,
    email_hash CHAR(64) NOT NULL,
    resume_token_encrypted TEXT NOT NULL,
    reminder_at DATETIME NULL,
    reminder_sent_at DATETIME NULL,
    resume_email_sent_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_session FOREIGN KEY (session_id) REFERENCES survey_sessions(id) ON DELETE CASCADE,
    INDEX idx_resume_due (reminder_at, reminder_sent_at),
    INDEX idx_resume_email_hash (email_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS participation_contacts (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NULL,
    email_encrypted TEXT NOT NULL,
    email_hash CHAR(64) NOT NULL,
    preferred_name_encrypted TEXT NULL,
    preferred_language VARCHAR(20) NOT NULL DEFAULT 'en',
    interests_json JSON NOT NULL,
    details_json JSON NULL,
    link_to_response TINYINT(1) NOT NULL DEFAULT 0,
    status ENUM('pending','active','unsubscribed') NOT NULL DEFAULT 'pending',
    verification_token_hash CHAR(64) NOT NULL UNIQUE,
    unsubscribe_token_hash CHAR(64) NOT NULL UNIQUE,
    unsubscribe_token_encrypted TEXT NOT NULL,
    consent_at DATETIME NOT NULL,
    verified_at DATETIME NULL,
    unsubscribed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_participation_email_hash (email_hash),
    INDEX idx_participation_status (status),
    CONSTRAINT fk_participation_session FOREIGN KEY (session_id) REFERENCES survey_sessions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
