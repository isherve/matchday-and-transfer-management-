package com.ferwafa.transfer.dto;

import com.ferwafa.common.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class TransferResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long teamFromId;
    private String teamFromName;
    private Long teamToId;
    private String teamToName;
    private String post;
    private LocalDate requestDate;
    private LocalDate approvalDate;
    private LocalDate rejectedDate;
    private LocalDate completedDate;
    private TransferStatus status;
}
