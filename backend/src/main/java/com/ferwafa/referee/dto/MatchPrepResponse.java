package com.ferwafa.referee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MatchPrepResponse {
    private Long id;
    private Long fixtureId;
    private Long refereeId;
    private boolean pitchChecked;
    private boolean ballsChecked;
    private boolean netsChecked;
    private boolean captainsBriefed;
    private boolean lineupsReceived;
    private boolean medicalReady;
    private boolean securityOk;
    private String notes;
    private int completedCount;
    private int totalCount;
    private LocalDateTime updatedAt;
}
