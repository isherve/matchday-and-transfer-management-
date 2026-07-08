package com.ferwafa.sanction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubSanctionRepository extends JpaRepository<ClubSanction, Long> {

    List<ClubSanction> findBySeasonAndActiveTrue(String season);

    List<ClubSanction> findByTeamTeamIdAndSeasonAndActiveTrue(Long teamId, String season);

    @Query("""
        SELECT COALESCE(SUM(s.pointsDeducted), 0) FROM ClubSanction s
        WHERE s.team.teamId = :teamId AND s.season = :season AND s.active = true
          AND s.sanctionType = com.ferwafa.common.ClubSanctionType.POINTS_DEDUCTION
        """)
    int sumActivePointsDeduction(@Param("teamId") Long teamId, @Param("season") String season);
}
