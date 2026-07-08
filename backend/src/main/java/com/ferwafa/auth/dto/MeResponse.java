package com.ferwafa.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MeResponse {
    private String role;
    private Long entityId;
    private String username;
    private String displayName;
}
