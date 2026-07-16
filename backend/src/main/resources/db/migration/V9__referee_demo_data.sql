-- Demo enrichment for referee Match Centre

-- Week 4 actionable fixtures for Jean Habimana (REF001)
INSERT INTO fixture (home_team_id, away_team_id, week, stadium, match_date, match_time, season, status, referee_id, created_at, updated_at)
SELECT 1, 3, 4, t.stadium, '2025-09-22', '15:00:00', '2025/26', 'REFEREE_ASSIGNED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM team t WHERE t.team_id = 1;

INSERT INTO fixture (home_team_id, away_team_id, week, stadium, match_date, match_time, season, status, referee_id, created_at, updated_at)
SELECT 2, 4, 4, t.stadium, '2025-09-24', '15:00:00', '2025/26', 'PLAYED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM team t WHERE t.team_id = 2;

INSERT INTO fixture (home_team_id, away_team_id, week, stadium, match_date, match_time, season, status, referee_id, created_at, updated_at)
SELECT 5, 6, 4, t.stadium, '2025-09-26', '17:00:00', '2025/26', 'REFEREE_ASSIGNED', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM team t WHERE t.team_id = 5;

INSERT INTO fixture (home_team_id, away_team_id, week, stadium, match_date, match_time, season, status, referee_id, home_score, away_score, created_at, updated_at)
SELECT 6, 8, 4, t.stadium, '2025-09-28', '15:00:00', '2025/26', 'REPORTED', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM team t WHERE t.team_id = 6;

-- Sample week-4 report (Etincelles vs AS Kigali)
INSERT INTO match_report (fixture_id, team_member_id, goal, goal_min, card, card_min, week, status, submitted_by_referee_id, created_at, updated_at)
SELECT f.id, m.member_id, 1, 34, 'NONE', NULL, 4, 'SUBMITTED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 6 AND m.number = 9 AND m.role_in_team = 'PLAYER'
WHERE f.week = 4 AND f.home_team_id = 6 AND f.away_team_id = 8;

INSERT INTO match_report (fixture_id, team_member_id, goal, goal_min, card, card_min, week, status, submitted_by_referee_id, created_at, updated_at)
SELECT f.id, m.member_id, 0, NULL, 'YELLOW', 61, 4, 'SUBMITTED', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 8 AND m.number = 6 AND m.role_in_team = 'PLAYER'
WHERE f.week = 4 AND f.home_team_id = 6 AND f.away_team_id = 8;

-- Referee notifications
INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 1, 'Assigned: APR FC vs Kiyovu Sports',
       'You are the match referee for Week 4 — kick-off 22 Sep 15:00.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 1, 'Match ready to report',
       'Rayon Sports vs Police FC is marked PLAYED. Submit the official match report.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 2 AND f.away_team_id = 4;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 1, 'Discipline check required',
       'Review suspensions before kick-off. Ineligible players must not start.',
       'PLAYER_SUSPENDED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 1, 'Matchday briefing',
       'Confirm pitch, nets, and match balls with the commissioner 60 minutes before KO.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 1 AND f.home_team_id = 1 LIMIT 1;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 2, 'Report awaiting approval',
       'Your Week 2 reports are with FERWAFA admin for approval.',
       'REPORT_SUBMITTED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 2 AND f.home_team_id = 2 LIMIT 1;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 2, 'Assigned: Etincelles vs AS Kigali',
       'Week 4 assignment — a sample report is already available for demo.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 4 AND f.home_team_id = 6 AND f.away_team_id = 8;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
SELECT 'REFEREE', 3, 'Week 3 assignments ready',
       'Four Week 3 fixtures are assigned to you. Open Match Centre to start reporting.',
       'REFEREE_ASSIGNED', 'FIXTURE', f.id, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f WHERE f.week = 3 AND f.referee_id = 3 LIMIT 1;

INSERT INTO notification (recipient_role, recipient_entity_id, title, message, type, related_entity_type, related_entity_id, read_flag, created_at, updated_at)
VALUES ('REFEREE', 3, 'Safety briefing',
        'Inspect pitch lighting and goal nets 45 minutes before kick-off.',
        'REFEREE_ASSIGNED', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Comments
INSERT INTO match_report_comment (fixture_id, body, author_role, author_name, author_entity_id, created_at)
SELECT id, 'Pitch surface was dry and playable. No postponement required.', 'REFEREE', 'Jean Habimana', 1, CURRENT_TIMESTAMP
FROM fixture WHERE week = 1 AND home_team_id = 1 AND away_team_id = 2 LIMIT 1;

INSERT INTO match_report_comment (fixture_id, body, author_role, author_name, author_entity_id, created_at)
SELECT id, 'Admin note: red card on #10 confirmed. Suspension applied for next fixture.', 'ADMIN', 'FERWAFA Administrator', 1, CURRENT_TIMESTAMP
FROM fixture WHERE week = 1 AND home_team_id = 1 AND away_team_id = 2 LIMIT 1;

INSERT INTO match_report_comment (fixture_id, body, author_role, author_name, author_entity_id, created_at)
SELECT id, 'Crowd behaviour was good. Minor delay at half-time for stretcher drill.', 'REFEREE', 'Patrick Niyonzima', 2, CURRENT_TIMESTAMP
FROM fixture WHERE week = 2 AND home_team_id = 2 AND away_team_id = 1 LIMIT 1;

INSERT INTO match_report_comment (fixture_id, body, author_role, author_name, author_entity_id, created_at)
SELECT id, 'Pre-match: both captains briefed. Awaiting kick-off.', 'REFEREE', 'Eric Muvunyi', 3, CURRENT_TIMESTAMP
FROM fixture WHERE week = 3 AND home_team_id = 1 AND away_team_id = 3 LIMIT 1;

INSERT INTO match_report_comment (fixture_id, body, author_role, author_name, author_entity_id, created_at)
SELECT id, 'Lineups received from both clubs. Pitch inspection complete.', 'REFEREE', 'Jean Habimana', 1, CURRENT_TIMESTAMP
FROM fixture WHERE week = 4 AND home_team_id = 1 AND away_team_id = 3 LIMIT 1;

-- Edit history
INSERT INTO match_report_edit_log (fixture_id, editor_role, editor_name, editor_entity_id, action, summary, created_at)
SELECT id, 'REFEREE', 'Jean Habimana', 1, 'SUBMITTED', 'Jean Habimana saved 4 report entries (2-1)', CURRENT_TIMESTAMP
FROM fixture WHERE week = 1 AND home_team_id = 1 LIMIT 1;

INSERT INTO match_report_edit_log (fixture_id, editor_role, editor_name, editor_entity_id, action, summary, created_at)
SELECT id, 'ADMIN', 'FERWAFA Administrator', 1, 'APPROVED', 'FERWAFA Administrator approved the match report (2-1)', CURRENT_TIMESTAMP
FROM fixture WHERE week = 1 AND home_team_id = 1 LIMIT 1;

INSERT INTO match_report_edit_log (fixture_id, editor_role, editor_name, editor_entity_id, action, summary, created_at)
SELECT id, 'REFEREE', 'Patrick Niyonzima', 2, 'SUBMITTED', 'Patrick Niyonzima saved 3 report entries (1-1)', CURRENT_TIMESTAMP
FROM fixture WHERE week = 2 AND home_team_id = 2 LIMIT 1;

INSERT INTO match_report_edit_log (fixture_id, editor_role, editor_name, editor_entity_id, action, summary, created_at)
SELECT id, 'ADMIN', 'FERWAFA Administrator', 1, 'UPDATED', 'FERWAFA Administrator corrected card minute on #7', CURRENT_TIMESTAMP
FROM fixture WHERE week = 2 AND home_team_id = 2 LIMIT 1;

INSERT INTO match_report_edit_log (fixture_id, editor_role, editor_name, editor_entity_id, action, summary, created_at)
SELECT id, 'REFEREE', 'Patrick Niyonzima', 2, 'SUBMITTED', 'Patrick Niyonzima saved 2 report entries (1-0)', CURRENT_TIMESTAMP
FROM fixture WHERE week = 4 AND home_team_id = 6 LIMIT 1;

-- Lineups for Week 4 demos
INSERT INTO lineup (fixture_id, team_id, member_id, created_at, updated_at)
SELECT f.id, 1, m.member_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 1 AND m.role_in_team = 'PLAYER' AND m.number BETWEEN 1 AND 11
WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO lineup (fixture_id, team_id, member_id, created_at, updated_at)
SELECT f.id, 3, m.member_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 3 AND m.role_in_team = 'PLAYER' AND m.number BETWEEN 1 AND 11
WHERE f.week = 4 AND f.home_team_id = 1 AND f.away_team_id = 3;

INSERT INTO lineup (fixture_id, team_id, member_id, created_at, updated_at)
SELECT f.id, 2, m.member_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 2 AND m.role_in_team = 'PLAYER' AND m.number BETWEEN 1 AND 11
WHERE f.week = 4 AND f.home_team_id = 2 AND f.away_team_id = 4;

INSERT INTO lineup (fixture_id, team_id, member_id, created_at, updated_at)
SELECT f.id, 4, m.member_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM fixture f
JOIN team_member m ON m.team_id = 4 AND m.role_in_team = 'PLAYER' AND m.number BETWEEN 1 AND 11
WHERE f.week = 4 AND f.home_team_id = 2 AND f.away_team_id = 4;
