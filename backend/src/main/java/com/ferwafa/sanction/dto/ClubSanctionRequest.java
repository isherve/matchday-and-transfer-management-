package com.ferwafa.sanction.dto;

import com.ferwafa.common.ClubSanctionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClubSanctionRequest {
    @NotNull
    private Long teamId;
    @NotBlank
    private String season;
    @NotNull
    private ClubSanctionType sanctionType;
    private Integer pointsDeducted;
    private BigDecimal fineAmount;
    private Integer stadiumBanMatches;
    @NotBlank
    private String reason;
    private Long commissionerReportId;
}
