package com.ferwafa.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MatchReportCommentRequest {
    @NotBlank
    @Size(max = 2000)
    private String body;
}
