package com.ferwafa.fixture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class PostponeFixtureRequest {
    @NotNull
    private LocalDate newMatchDate;
    private LocalTime newMatchTime;
    @NotBlank
    private String reason;
}
