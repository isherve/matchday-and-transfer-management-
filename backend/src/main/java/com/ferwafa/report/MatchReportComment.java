package com.ferwafa.report;

import com.ferwafa.fixture.Fixture;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_report_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchReportComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "author_role", nullable = false, length = 20)
    private String authorRole;

    @Column(name = "author_name", nullable = false, length = 120)
    private String authorName;

    @Column(name = "author_entity_id")
    private Long authorEntityId;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
