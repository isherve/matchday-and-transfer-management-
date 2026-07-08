package com.ferwafa.standings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LeaderboardEntry {
    private int rank;
    private Long memberId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private int goals;
    private int yellowCards;
    private int redCards;
}
