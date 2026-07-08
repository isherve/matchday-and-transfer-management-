package com.ferwafa.fixture.dto;

import com.ferwafa.common.FixtureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class FixtureResponse {
    private Long id;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private Integer week;
    private String stadium;
    private LocalDate matchDate;
    private LocalTime matchTime;
    private String season;
    private FixtureStatus status;
    private Long refereeId;
    private String refereeName;
    private Integer homeScore;
    private Integer awayScore;
    private String postponementReason;
    private LocalDate originalMatchDate;
}
