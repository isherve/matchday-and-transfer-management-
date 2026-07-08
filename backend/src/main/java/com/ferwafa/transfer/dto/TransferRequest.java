package com.ferwafa.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull
    private Long memberId;
    @NotNull
    private Long teamFromId;
    @NotNull
    private Long teamToId;
    private String post;
}
