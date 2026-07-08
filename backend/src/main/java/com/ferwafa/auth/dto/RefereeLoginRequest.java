package com.ferwafa.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefereeLoginRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String accessCode;
}
