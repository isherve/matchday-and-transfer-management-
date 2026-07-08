package com.ferwafa.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class TransferWindowResponse {
    private Long id;
    private String season;
    private String name;
    private LocalDate openDate;
    private LocalDate closeDate;
    private boolean active;
    private boolean currentlyOpen;
}
