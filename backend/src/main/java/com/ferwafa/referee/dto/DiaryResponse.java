package com.ferwafa.referee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class DiaryResponse {
    private Long id;
    private String title;
    private String body;
    private LocalDate entryDate;
    private LocalDateTime createdAt;
}
