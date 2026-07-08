package com.ferwafa.fixture.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LineupResponse {
    private Long id;
    private Long fixtureId;
    private Long teamId;
    private Long memberId;
    private String playerName;
    private Integer playerNumber;
    private String position;
    private boolean suspended;
    private String suspensionReason;
}
