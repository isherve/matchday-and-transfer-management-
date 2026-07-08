package com.ferwafa.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TeamResponse {
    private Long teamId;
    private String name;
    private String logo;
    private String stadium;
    private String username;
}
