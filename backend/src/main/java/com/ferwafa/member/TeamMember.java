package com.ferwafa.member;

import jakarta.persistence.*;
import lombok.*;
import com.ferwafa.common.AuditableEntity;
import com.ferwafa.common.MemberRole;
import com.ferwafa.team.Team;

@Entity
@Table(name = "team_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMember extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false)
    private String fname;

    @Column(nullable = false)
    private String lname;

    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_team", nullable = false)
    private MemberRole roleInTeam;

    private String post;

    private String position;

    private String contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
