package com.ferwafa.referee;

import com.ferwafa.fixture.Fixture;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referee_match_prep")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefereeMatchPrep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Column(name = "referee_id", nullable = false)
    private Long refereeId;

    @Builder.Default
    @Column(name = "pitch_checked", nullable = false)
    private boolean pitchChecked = false;

    @Builder.Default
    @Column(name = "balls_checked", nullable = false)
    private boolean ballsChecked = false;

    @Builder.Default
    @Column(name = "nets_checked", nullable = false)
    private boolean netsChecked = false;

    @Builder.Default
    @Column(name = "captains_briefed", nullable = false)
    private boolean captainsBriefed = false;

    @Builder.Default
    @Column(name = "lineups_received", nullable = false)
    private boolean lineupsReceived = false;

    @Builder.Default
    @Column(name = "medical_ready", nullable = false)
    private boolean medicalReady = false;

    @Builder.Default
    @Column(name = "security_ok", nullable = false)
    private boolean securityOk = false;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
