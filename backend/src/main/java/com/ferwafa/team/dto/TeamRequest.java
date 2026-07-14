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
    /** Required on create; optional on update (leave blank to keep current password). */
    private String password;
}
