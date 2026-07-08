package com.ferwafa.fixture;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.member.TeamMember;
import com.ferwafa.team.Team;

@Entity
@Table(name = "lineup", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fixture_id", "team_id", "member_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lineup extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private TeamMember member;
}
