package com.ferwafa.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamTeamId(Long teamId);
    List<TeamMember> findByTeamTeamIdAndRoleInTeam(Long teamId, com.ferwafa.common.MemberRole role);
}
