package com.ferwafa.referee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RefereeResponse {
    private Long refereeId;
    private String fname;
    private String lname;
    private String image;
    private String email;
}
