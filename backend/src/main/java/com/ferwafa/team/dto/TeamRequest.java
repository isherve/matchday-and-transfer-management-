package com.ferwafa.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamRequest {
    @NotBlank
    private String name;
    private String logo;
    private String stadium;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
