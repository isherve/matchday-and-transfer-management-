package com.ferwafa.referee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefereeRequest {
    @NotBlank
    private String fname;
    @NotBlank
    private String lname;
    private String image;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String accessCode;
}
