package com.ferwafa.sanction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CommissionerReportResponse {
    private Long id;
    private Long fixtureId;
    private String matchLabel;
    private String pitchCondition;
    private String crowdBehavior;
    private String securityIncidents;
    private String technicalIssues;
    private String otherNotes;
    private String status;
}
