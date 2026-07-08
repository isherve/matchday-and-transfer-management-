package com.ferwafa.sanction;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.auth.User;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.ClubSanctionType;
import com.ferwafa.team.Team;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "club_sanction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubSanction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String season;

    @Enumerated(EnumType.STRING)
    @Column(name = "sanction_type", nullable = false)
    private ClubSanctionType sanctionType;

    @Column(name = "points_deducted", nullable = false)
    @Builder.Default
    private Integer pointsDeducted = 0;

    @Column(name = "fine_amount")
    private BigDecimal fineAmount;

    @Column(name = "stadium_ban_matches")
    private Integer stadiumBanMatches;

    @Column(nullable = false, length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commissioner_report_id")
    private CommissionerReport commissionerReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by_admin_id")
    private User issuedByAdmin;

    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
