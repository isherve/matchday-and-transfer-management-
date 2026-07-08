-- Phase 12-17: Match results, transfer windows, club sanctions, commissioner reports, notifications

ALTER TABLE fixture ADD COLUMN home_score INT NULL;
ALTER TABLE fixture ADD COLUMN away_score INT NULL;
ALTER TABLE fixture ADD COLUMN postponement_reason VARCHAR(500) NULL;
ALTER TABLE fixture ADD COLUMN original_match_date DATE NULL;

CREATE TABLE transfer_window (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    season VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    open_date DATE NOT NULL,
    close_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_transfer_window_season (season, active)
);

CREATE TABLE commissioner_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    submitted_by_admin_id BIGINT,
    pitch_condition VARCHAR(50),
    crowd_behavior VARCHAR(50),
    security_incidents TEXT,
    technical_issues TEXT,
    other_notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comm_report_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_comm_report_admin FOREIGN KEY (submitted_by_admin_id) REFERENCES users(id),
    CONSTRAINT uk_comm_report_fixture UNIQUE (fixture_id)
);

CREATE TABLE club_sanction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    season VARCHAR(20) NOT NULL,
    sanction_type VARCHAR(30) NOT NULL,
    points_deducted INT NOT NULL DEFAULT 0,
    fine_amount DECIMAL(12,2),
    stadium_ban_matches INT,
    reason VARCHAR(500) NOT NULL,
    commissioner_report_id BIGINT,
    issued_by_admin_id BIGINT,
    issued_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_club_sanction_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    CONSTRAINT fk_club_sanction_report FOREIGN KEY (commissioner_report_id) REFERENCES commissioner_report(id),
    CONSTRAINT fk_club_sanction_admin FOREIGN KEY (issued_by_admin_id) REFERENCES users(id)
);

CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_role VARCHAR(20) NOT NULL,
    recipient_entity_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(40) NOT NULL,
    related_entity_type VARCHAR(40),
    related_entity_id BIGINT,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE league_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(50) NOT NULL,
    rule_value VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_league_rule_key UNIQUE (rule_key)
);

INSERT INTO transfer_window (season, name, open_date, close_date, active, created_at, updated_at)
VALUES
    ('2025/26', 'Summer Window', '2025-06-01', '2025-09-30', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2025/26', 'Winter Window', '2026-01-01', '2026-01-31', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('2025/26', 'Special Mid-Season Window', '2026-07-01', '2026-07-31', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_rule (rule_key, rule_value, description, created_at, updated_at) VALUES
    ('MAX_SQUAD_SIZE', '30', 'Maximum registered players per club', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MIN_SQUAD_SIZE', '14', 'Minimum registered players per club', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MAX_LINEUP_SIZE', '11', 'Maximum players in starting lineup', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('MIN_PLAYER_AGE', '16', 'Minimum age for player registration (years)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

UPDATE fixture SET home_score = 2, away_score = 1 WHERE id = 1 AND status = 'APPROVED';
