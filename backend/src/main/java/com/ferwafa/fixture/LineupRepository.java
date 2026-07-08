package com.ferwafa.fixture;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LineupRepository extends JpaRepository<Lineup, Long> {
    List<Lineup> findByFixtureIdAndTeamTeamId(Long fixtureId, Long teamId);
    List<Lineup> findByFixtureId(Long fixtureId);
    boolean existsByFixtureIdAndTeamTeamIdAndMemberMemberId(Long fixtureId, Long teamId, Long memberId);
    Optional<Lineup> findByFixtureIdAndTeamTeamIdAndMemberMemberId(Long fixtureId, Long teamId, Long memberId);
}
