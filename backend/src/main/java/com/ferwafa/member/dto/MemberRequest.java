package com.ferwafa.member.dto;

import com.ferwafa.common.MemberRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRequest {
    @NotBlank
    private String fname;
    @NotBlank
    private String lname;
    private Integer number;
    @NotNull
    private MemberRole roleInTeam;
    private String post;
    private String position;
    private String contract;
}
