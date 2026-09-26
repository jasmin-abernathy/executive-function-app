CREATE TABLE IF NOT EXISTS resosoin_survey_sessions (
  id CHAR(36) NOT NULL PRIMARY KEY,
  audience VARCHAR(16) NOT NULL,
  resume_token_hash CHAR(64) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL DEFAULT 'active',
  current_step VARCHAR(100) NULL,
  survey_version VARCHAR(40) NOT NULL DEFAULT '2026-09-26-v3',
  recruitment_source VARCHAR(32) NOT NULL DEFAULT 'direct',
  professional_verified TINYINT(1) NOT NULL DEFAULT 0,
  professional_verification_method VARCHAR(64) NULL,
  consent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  submitted_at DATETIME NULL,
  INDEX idx_resosoin_audience_status (audience, status),
  INDEX idx_resosoin_updated (updated_at),
  INDEX idx_resosoin_source (recruitment_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resosoin_survey_answers (
  session_id CHAR(36) NOT NULL,
  question_id VARCHAR(100) NOT NULL,
  answer_json JSON NOT NULL,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (session_id, question_id),
  CONSTRAINT fk_resosoin_answer_session
    FOREIGN KEY (session_id) REFERENCES resosoin_survey_sessions(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
