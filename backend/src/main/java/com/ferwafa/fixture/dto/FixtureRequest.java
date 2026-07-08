package com.ferwafa.fixture.dto;

import com.ferwafa.common.FixtureStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class FixtureRequest {
    @NotNull
    private Long homeTeamId;
    @NotNull
    private Long awayTeamId;
    @NotNull
    private Integer week;
    private String stadium;
    @NotNull
    private LocalDate matchDate;
    @NotNull
    private LocalTime matchTime;
    @NotBlank
    private String season;
}
