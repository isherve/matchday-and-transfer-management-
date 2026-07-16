package com.ferwafa.referee.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MatchPrepRequest {
    private boolean pitchChecked;
    private boolean ballsChecked;
    private boolean netsChecked;
    private boolean captainsBriefed;
    private boolean lineupsReceived;
    private boolean medicalReady;
    private boolean securityOk;
    @Size(max = 1000)
    private String notes;
}
