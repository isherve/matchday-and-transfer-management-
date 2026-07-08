package com.ferwafa.report.dto;

import com.ferwafa.common.CardType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MatchReportEntryRequest {
    @NotNull
    private Long teamMemberId;
    private Integer goal = 0;
    private Integer goalMin;
    private CardType card = CardType.NONE;
    private Integer cardMin;
}
