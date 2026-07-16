package com.ferwafa.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MatchReportCommentResponse {
    private Long id;
    private Long fixtureId;
    private String body;
    private String authorRole;
    private String authorName;
    private Long authorEntityId;
    private LocalDateTime createdAt;
}
