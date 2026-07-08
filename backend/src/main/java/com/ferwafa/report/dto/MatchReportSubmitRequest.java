package com.ferwafa.report.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MatchReportSubmitRequest {
    /** May be empty for a 0-0 match with no cards. */
    @NotNull
    @Valid
    private List<MatchReportEntryRequest> entries = new ArrayList<>();
}
