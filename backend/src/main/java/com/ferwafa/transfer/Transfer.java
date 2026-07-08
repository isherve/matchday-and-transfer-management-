package com.ferwafa.transfer;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.TransferStatus;
import com.ferwafa.member.TeamMember;
import com.ferwafa.team.Team;

import java.time.LocalDate;

@Entity
@Table(name = "transfer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private TeamMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_from_id", nullable = false)
    private Team teamFrom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_to_id", nullable = false)
    private Team teamTo;

    private String post;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "rejected_date")
    private LocalDate rejectedDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransferStatus status = TransferStatus.REQUESTED;
}
