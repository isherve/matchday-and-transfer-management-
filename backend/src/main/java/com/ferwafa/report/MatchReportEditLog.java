package com.ferwafa.report;

import com.ferwafa.fixture.Fixture;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_report_edit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchReportEditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Column(name = "editor_role", nullable = false, length = 20)
    private String editorRole;

    @Column(name = "editor_name", nullable = false, length = 120)
    private String editorName;

    @Column(name = "editor_entity_id")
    private Long editorEntityId;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
