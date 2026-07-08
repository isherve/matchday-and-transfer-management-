package com.ferwafa.fixture.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LineupRequest {
    @NotNull
    private Long teamId;
    @NotEmpty
    private List<Long> memberIds;
}
