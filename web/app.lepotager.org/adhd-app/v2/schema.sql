CREATE TABLE IF NOT EXISTS survey_v2_guardian_consents (
  id CHAR(36) NOT NULL PRIMARY KEY,
  email_encrypted TEXT NULL,
  email_hash CHAR(64) NOT NULL,
  confirm_token_hash CHAR(64) NOT NULL UNIQUE,
  decline_token_hash CHAR(64) NOT NULL UNIQUE,
  revoke_token_hash CHAR(64) NOT NULL UNIQUE,
  poll_token_hash CHAR(64) NOT NULL UNIQUE,
  status ENUM('pending','confirmed','declined','revoked','expired') NOT NULL DEFAULT 'pending',
  child_assent_at DATETIME NOT NULL,
  consent_text_version VARCHAR(40) NOT NULL,
  survey_session_id CHAR(36) NULL,
  requested_at DATETIME NOT NULL,
  confirmed_at DATETIME NULL,
  updated_at DATETIME NOT NULL,
  expires_at DATETIME NOT NULL,
  INDEX idx_v2_guardian_status (status),
  INDEX idx_v2_guardian_email_hash (email_hash),
  INDEX idx_v2_guardian_session (survey_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS survey_v2_sessions (
  id CHAR(36) NOT NULL PRIMARY KEY,
  resume_token_hash CHAR(64) NOT NULL UNIQUE,
  cohort ENUM('adult','minor') NOT NULL,
  status ENUM('active','core_submitted','completed','deleted') NOT NULL DEFAULT 'active',
  current_step VARCHAR(100) NOT NULL,
  guardian_consent_id CHAR(36) NULL,
  consent_text_version VARCHAR(40) NOT NULL,
  participant_consent_at DATETIME NOT NULL,
  core_submitted_at DATETIME NULL,
  completed_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uniq_v2_guardian_consent (guardian_consent_id),
  INDEX idx_v2_session_status (status),
  INDEX idx_v2_session_step (current_step),
  INDEX idx_v2_session_updated (updated_at),
  CONSTRAINT fk_v2_session_guardian
    FOREIGN KEY (guardian_consent_id)
    REFERENCES survey_v2_guardian_consents(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS survey_v2_answers (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  session_id CHAR(36) NOT NULL,
  question_id VARCHAR(100) NOT NULL,
  answer_json JSON NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uniq_v2_session_question (session_id, question_id),
  INDEX idx_v2_answer_question (question_id),
  CONSTRAINT fk_v2_answer_session
    FOREIGN KEY (session_id)
    REFERENCES survey_v2_sessions(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
