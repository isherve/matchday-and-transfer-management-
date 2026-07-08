-- FERWAFA Match Day System - Initial Schema

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fname VARCHAR(100) NOT NULL,
    lname VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE team (
    team_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    logo VARCHAR(255),
    stadium VARCHAR(150),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_team_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE referee (
    referee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fname VARCHAR(100) NOT NULL,
    lname VARCHAR(100) NOT NULL,
    image VARCHAR(255),
    email VARCHAR(150) NOT NULL UNIQUE,
    access_code_hash VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_referee_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE team_member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fname VARCHAR(100) NOT NULL,
    lname VARCHAR(100) NOT NULL,
    number INT,
    role_in_team VARCHAR(20) NOT NULL DEFAULT 'PLAYER',
    post VARCHAR(50),
    position VARCHAR(50),
    contract VARCHAR(100),
    team_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    INDEX idx_member_team (team_id),
    INDEX idx_member_role (role_in_team)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE fixture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    week INT NOT NULL,
    stadium VARCHAR(150),
    match_date DATE NOT NULL,
    match_time TIME NOT NULL,
    season VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    referee_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_fixture_home FOREIGN KEY (home_team_id) REFERENCES team(team_id),
    CONSTRAINT fk_fixture_away FOREIGN KEY (away_team_id) REFERENCES team(team_id),
    CONSTRAINT fk_fixture_referee FOREIGN KEY (referee_id) REFERENCES referee(referee_id),
    INDEX idx_fixture_week (week, season),
    INDEX idx_fixture_status (status),
    INDEX idx_fixture_referee (referee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE match_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    team_member_id BIGINT NOT NULL,
    goal INT NOT NULL DEFAULT 0,
    goal_min INT,
    card VARCHAR(10) NOT NULL DEFAULT 'NONE',
    card_min INT,
    week INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_by_referee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_report_member FOREIGN KEY (team_member_id) REFERENCES team_member(member_id),
    CONSTRAINT fk_report_referee FOREIGN KEY (submitted_by_referee_id) REFERENCES referee(referee_id),
    INDEX idx_report_fixture (fixture_id),
    INDEX idx_report_member (team_member_id),
    INDEX idx_report_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    team_from_id BIGINT NOT NULL,
    team_to_id BIGINT NOT NULL,
    post VARCHAR(50),
    request_date DATE NOT NULL,
    approval_date DATE,
    rejected_date DATE,
    completed_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transfer_member FOREIGN KEY (member_id) REFERENCES team_member(member_id),
    CONSTRAINT fk_transfer_from FOREIGN KEY (team_from_id) REFERENCES team(team_id),
    CONSTRAINT fk_transfer_to FOREIGN KEY (team_to_id) REFERENCES team(team_id),
    INDEX idx_transfer_status (status),
    INDEX idx_transfer_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lineup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lineup_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_lineup_team FOREIGN KEY (team_id) REFERENCES team(team_id),
    CONSTRAINT fk_lineup_member FOREIGN KEY (member_id) REFERENCES team_member(member_id),
    UNIQUE KEY uk_lineup_fixture_team_member (fixture_id, team_id, member_id),
    INDEX idx_lineup_fixture_team (fixture_id, team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disciplinary_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_member_id BIGINT NOT NULL,
    fixture_id BIGINT NOT NULL,
    week INT NOT NULL,
    card_type VARCHAR(10) NOT NULL,
    card_min INT,
    suspension_reason VARCHAR(20),
    suspension_fixture_id BIGINT,
    suspension_served BOOLEAN NOT NULL DEFAULT FALSE,
    served_fixture_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_disc_member FOREIGN KEY (team_member_id) REFERENCES team_member(member_id),
    CONSTRAINT fk_disc_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_disc_suspension_fixture FOREIGN KEY (suspension_fixture_id) REFERENCES fixture(id),
    CONSTRAINT fk_disc_served_fixture FOREIGN KEY (served_fixture_id) REFERENCES fixture(id),
    INDEX idx_disc_member (team_member_id),
    INDEX idx_disc_suspension (suspension_fixture_id, suspension_served)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
