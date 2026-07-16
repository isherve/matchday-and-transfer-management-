package com.ferwafa.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MatchReportEditLogResponse {
    private Long id;
    private Long fixtureId;
    private String editorRole;
    private String editorName;
    private Long editorEntityId;
    private String action;
    private String summary;
    private LocalDateTime createdAt;
}
