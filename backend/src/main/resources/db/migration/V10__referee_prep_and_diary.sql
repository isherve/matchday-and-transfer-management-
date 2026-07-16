CREATE TABLE referee_match_prep (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    referee_id BIGINT NOT NULL,
    pitch_checked BOOLEAN NOT NULL DEFAULT FALSE,
    balls_checked BOOLEAN NOT NULL DEFAULT FALSE,
    nets_checked BOOLEAN NOT NULL DEFAULT FALSE,
    captains_briefed BOOLEAN NOT NULL DEFAULT FALSE,
    lineups_received BOOLEAN NOT NULL DEFAULT FALSE,
    medical_ready BOOLEAN NOT NULL DEFAULT FALSE,
    security_ok BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prep_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_prep_referee FOREIGN KEY (referee_id) REFERENCES referee(referee_id),
    CONSTRAINT uk_prep_fixture_referee UNIQUE (fixture_id, referee_id)
);

CREATE TABLE referee_diary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    referee_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    body VARCHAR(2000) NOT NULL,
    entry_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_diary_referee FOREIGN KEY (referee_id) REFERENCES referee(referee_id),
    INDEX idx_diary_referee (referee_id)
);

-- Prep data for Jean Habimana week 4 + week 1
INSERT INTO referee_match_prep (fixture_id, referee_id, pitch_checked, balls_checked, nets_checked, captains_briefed, lineups_received, medical_ready, security_ok, notes, created_at, updated_at)
SELECT f.id, 1, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE,
       'Waiting on security briefing from stadium manager.',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO referee_match_prep (fixture_id, referee_id, pitch_checked, balls_checked, nets_checked, captains_briefed, lineups_received, medical_ready, security_ok, notes, created_at, updated_at)
SELECT f.id, 1, TRUE, TRUE, TRUE, FALSE, FALSE, TRUE, TRUE,
       'Arrive 75 minutes before KO. Confirm police detail.',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 2 AND f.away_team_id = 4;

INSERT INTO referee_match_prep (fixture_id, referee_id, pitch_checked, balls_checked, nets_checked, captains_briefed, lineups_received, medical_ready, security_ok, notes, created_at, updated_at)
SELECT f.id, 1, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE,
       'Match completed — archive checklist.',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 1 AND f.home_team_id = 1 AND f.away_team_id = 2 LIMIT 1;

INSERT INTO referee_match_prep (fixture_id, referee_id, pitch_checked, balls_checked, nets_checked, captains_briefed, lineups_received, medical_ready, security_ok, notes, created_at, updated_at)
SELECT f.id, 3, TRUE, TRUE, FALSE, FALSE, FALSE, TRUE, TRUE,
       'Nets need re-check after warm-up.',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 3 AND f.referee_id = 3 LIMIT 1;

-- Diary entries (Account / Stats personal log)
INSERT INTO referee_diary (referee_id, title, body, entry_date, created_at, updated_at) VALUES
(1, 'Season opener notes', 'Strong cooperation from both clubs. Focus this month: consistent advantage play and clear card signals.', '2025-09-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Week 4 preparation', 'Three assignments this week. Prioritise APR vs Kiyovu lineups and Rayon vs Police report after PLAYED status.', '2025-09-21', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Fitness reminder', 'Completed FIFA fitness session. Ready for evening kick-offs.', '2025-09-20', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Reports pending approval', 'Submitted Week 2 reports. Follow up with admin if not approved by Friday.', '2025-09-12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Week 3 slate', 'Four fixtures assigned. Start with APR vs Kiyovu pre-match checklist.', '2025-09-14', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Extra alerts so Alerts tab is full for demo
INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 1, 'Checklist reminder',
       'Complete matchday prep for APR FC vs Kiyovu Sports before arrival.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
VALUES
('REFEREE', 1, 'Diary tip', 'Log personal match notes under Account → Duty diary for your records.', 'REFEREE_ASSIGNED', NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
