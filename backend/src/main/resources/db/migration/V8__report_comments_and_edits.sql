CREATE TABLE match_report_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    body TEXT NOT NULL,
    author_role VARCHAR(20) NOT NULL,
    author_name VARCHAR(120) NOT NULL,
    author_entity_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_comment_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    INDEX idx_report_comment_fixture (fixture_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE match_report_edit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fixture_id BIGINT NOT NULL,
    editor_role VARCHAR(20) NOT NULL,
    editor_name VARCHAR(120) NOT NULL,
    editor_entity_id BIGINT,
    action VARCHAR(40) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_edit_fixture FOREIGN KEY (fixture_id) REFERENCES fixture(id),
    INDEX idx_report_edit_fixture (fixture_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
