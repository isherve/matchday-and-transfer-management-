package com.ferwafa.fixture.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRefereeRequest {
    @NotNull
    private Long refereeId;
}
