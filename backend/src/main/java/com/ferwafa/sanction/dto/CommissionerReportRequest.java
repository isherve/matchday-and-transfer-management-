package com.ferwafa.sanction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommissionerReportRequest {
    @NotNull
    private Long fixtureId;
    private String pitchCondition;
    private String crowdBehavior;
    private String securityIncidents;
    private String technicalIssues;
    private String otherNotes;
}
