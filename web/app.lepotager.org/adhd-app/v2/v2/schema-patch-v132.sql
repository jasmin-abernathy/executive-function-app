-- Optional one-shot V2 resume reminders (v1.3.2)
CREATE TABLE IF NOT EXISTS survey_v2_resume_reminders (
  id CHAR(36) NOT NULL PRIMARY KEY,
  session_id CHAR(36) NOT NULL,
  email_encrypted TEXT NOT NULL,
  email_hash CHAR(64) NOT NULL,
  resume_token_encrypted TEXT NOT NULL,
  language ENUM('fr','en') NOT NULL DEFAULT 'en',
  reminder_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uniq_v2_reminder_session (session_id),
  INDEX idx_v2_reminder_due (reminder_at),
  INDEX idx_v2_reminder_email_hash (email_hash),
  CONSTRAINT fk_v2_reminder_session
    FOREIGN KEY (session_id) REFERENCES survey_v2_sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
