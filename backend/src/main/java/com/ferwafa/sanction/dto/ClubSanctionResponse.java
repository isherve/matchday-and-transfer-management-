package com.ferwafa.sanction.dto;

import com.ferwafa.common.ClubSanctionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class ClubSanctionResponse {
    private Long id;
    private Long teamId;
    private String teamName;
    private String season;
    private ClubSanctionType sanctionType;
    private Integer pointsDeducted;
    private BigDecimal fineAmount;
    private Integer stadiumBanMatches;
    private String reason;
    private Long commissionerReportId;
    private LocalDate issuedDate;
    private boolean active;
}
