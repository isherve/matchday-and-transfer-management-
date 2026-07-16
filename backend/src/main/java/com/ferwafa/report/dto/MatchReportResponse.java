package com.ferwafa.report.dto;

import com.ferwafa.common.CardType;
import com.ferwafa.common.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MatchReportResponse {
    private Long reportId;
    private Long fixtureId;
    private String matchLabel;
    private Integer homeScore;
    private Integer awayScore;
    private Long teamMemberId;
    private String playerName;
    private String teamName;
    private Integer goal;
    private Integer goalMin;
    private CardType card;
    private Integer cardMin;
    private Integer week;
    private ReportStatus status;
    private Long submittedByRefereeId;
    private String refereeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
