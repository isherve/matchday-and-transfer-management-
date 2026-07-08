package com.ferwafa.fixture;

import com.ferwafa.common.FixtureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FixtureRepository extends JpaRepository<Fixture, Long> {
    List<Fixture> findBySeasonOrderByWeekAscMatchDateAsc(String season);
    List<Fixture> findByRefereeRefereeIdOrderByMatchDateAsc(Long refereeId);
    List<Fixture> findByHomeTeamTeamIdOrAwayTeamTeamIdOrderByMatchDateAsc(Long homeTeamId, Long awayTeamId);

    @Query("SELECT f FROM Fixture f WHERE (f.homeTeam.teamId = :teamId OR f.awayTeam.teamId = :teamId) AND f.season = :season ORDER BY f.week ASC")
    List<Fixture> findByTeamAndSeason(@Param("teamId") Long teamId, @Param("season") String season);

    @Query("SELECT f FROM Fixture f WHERE (f.homeTeam.teamId = :teamId OR f.awayTeam.teamId = :teamId) AND f.week = :week AND f.season = :season")
    List<Fixture> findTeamFixturesForWeek(@Param("teamId") Long teamId, @Param("week") Integer week, @Param("season") String season);

    @Query("SELECT f FROM Fixture f WHERE (f.homeTeam.teamId = :teamId OR f.awayTeam.teamId = :teamId) AND f.week > :week AND f.season = :season ORDER BY f.week ASC")
    List<Fixture> findNextFixturesForTeam(@Param("teamId") Long teamId, @Param("week") Integer week, @Param("season") String season);

    List<Fixture> findByStatus(FixtureStatus status);
}
