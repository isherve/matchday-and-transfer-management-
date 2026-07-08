package com.ferwafa.fixture;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.FixtureStatus;
import com.ferwafa.referee.Referee;
import com.ferwafa.team.Team;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "fixture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fixture extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @Column(nullable = false)
    private Integer week;

    private String stadium;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "match_time", nullable = false)
    private LocalTime matchTime;

    @Column(nullable = false)
    private String season;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FixtureStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id")
    private Referee referee;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(name = "postponement_reason", length = 500)
    private String postponementReason;

    @Column(name = "original_match_date")
    private LocalDate originalMatchDate;
}
