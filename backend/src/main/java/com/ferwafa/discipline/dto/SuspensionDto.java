package com.ferwafa.discipline.dto;

import com.ferwafa.common.SuspensionReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SuspensionDto {
    private Long memberId;
    private String playerName;
    private Integer playerNumber;
    private SuspensionReason reason;
    private String reasonLabel;
    private Long triggeringFixtureId;
    private String triggeringMatch;
    private Integer triggeringWeek;
}
