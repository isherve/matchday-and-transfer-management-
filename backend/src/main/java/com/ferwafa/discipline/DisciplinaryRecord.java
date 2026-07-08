package com.ferwafa.discipline;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.CardType;
import com.ferwafa.common.SuspensionReason;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.member.TeamMember;

@Entity
@Table(name = "disciplinary_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisciplinaryRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @Column(nullable = false)
    private Integer week;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "card_min")
    private Integer cardMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "suspension_reason")
    private SuspensionReason suspensionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspension_fixture_id")
    private Fixture suspensionFixture;

    @Column(name = "suspension_served", nullable = false)
    @Builder.Default
    private Boolean suspensionServed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "served_fixture_id")
    private Fixture servedFixture;
}
