package com.ferwafa.report;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.CardType;
import com.ferwafa.common.ReportStatus;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.member.TeamMember;
import com.ferwafa.referee.Referee;

@Entity
@Table(name = "match_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    @Column(nullable = false)
    @Builder.Default
    private Integer goal = 0;

    @Column(name = "goal_min")
    private Integer goalMin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CardType card = CardType.NONE;

    @Column(name = "card_min")
    private Integer cardMin;

    @Column(nullable = false)
    private Integer week;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_referee_id", nullable = false)
    private Referee submittedByReferee;
}
