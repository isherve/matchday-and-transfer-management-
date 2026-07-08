package com.ferwafa.sanction;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.auth.User;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.fixture.Fixture;

@Entity
@Table(name = "commissioner_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionerReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false, unique = true)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_admin_id")
    private User submittedByAdmin;

    @Column(name = "pitch_condition")
    private String pitchCondition;

    @Column(name = "crowd_behavior")
    private String crowdBehavior;

    @Column(name = "security_incidents", columnDefinition = "TEXT")
    private String securityIncidents;

    @Column(name = "technical_issues", columnDefinition = "TEXT")
    private String technicalIssues;

    @Column(name = "other_notes", columnDefinition = "TEXT")
    private String otherNotes;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SUBMITTED";
}
