package com.ferwafa.member.dto;

import com.ferwafa.common.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberResponse {
    private Long memberId;
    private String fname;
    private String lname;
    private Integer number;
    private MemberRole roleInTeam;
    private String post;
    private String position;
    private String contract;
    private Long teamId;
    private String teamName;
}
