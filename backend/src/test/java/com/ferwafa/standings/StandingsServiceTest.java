package com.ferwafa.standings;

import com.ferwafa.common.FixtureStatus;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.referee.Referee;
import com.ferwafa.referee.RefereeRepository;
import com.ferwafa.sanction.ClubSanction;
import com.ferwafa.sanction.ClubSanctionRepository;
import com.ferwafa.standings.dto.StandingRow;
import com.ferwafa.team.Team;
import com.ferwafa.team.TeamRepository;
import com.ferwafa.common.ClubSanctionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StandingsServiceTest {

    @Autowired private StandingsService standingsService;
    @Autowired private TeamRepository teamRepository;
    @Autowired private FixtureRepository fixtureRepository;
    @Autowired private RefereeRepository refereeRepository;
    @Autowired private ClubSanctionRepository clubSanctionRepository;

    private Team home;
    private Team away;
    private String season = "TS-" + (System.nanoTime() % 1_000_000);

    @BeforeEach
    void setUp() {
        home = teamRepository.save(Team.builder().name("Alpha FC")
                .username("alpha_" + System.nanoTime()).passwordHash("h").build());
        away = teamRepository.save(Team.builder().name("Beta FC")
                .username("beta_" + System.nanoTime()).passwordHash("h").build());
        Referee ref = refereeRepository.save(Referee.builder().fname("R").lname("T")
                .email("ref_stand_" + System.nanoTime() + "@t.com").accessCodeHash("h").build());

        fixtureRepository.save(Fixture.builder()
                .homeTeam(home).awayTeam(away).week(1).season(season)
                .matchDate(LocalDate.now()).matchTime(LocalTime.NOON)
                .status(FixtureStatus.APPROVED).referee(ref)
                .homeScore(2).awayScore(1).build());
    }

    @Test
    void computesPointsFromFixtureScores() {
        List<StandingRow> table = standingsService.getStandings(season);
        StandingRow alpha = table.stream().filter(r -> r.getTeamId().equals(home.getTeamId())).findFirst().orElseThrow();
        StandingRow beta = table.stream().filter(r -> r.getTeamId().equals(away.getTeamId())).findFirst().orElseThrow();

        assertThat(alpha.getPoints()).isEqualTo(3);
        assertThat(alpha.getWon()).isEqualTo(1);
        assertThat(alpha.getGoalsFor()).isEqualTo(2);
        assertThat(beta.getPoints()).isEqualTo(0);
        assertThat(beta.getLost()).isEqualTo(1);
        assertThat(alpha.getPosition()).isLessThan(beta.getPosition());
    }

    @Test
    void appliesClubPointsDeduction() {
        clubSanctionRepository.save(ClubSanction.builder()
                .team(home).season(season).sanctionType(ClubSanctionType.POINTS_DEDUCTION)
                .pointsDeducted(3).reason("Crowd misconduct").issuedDate(LocalDate.now()).active(true).build());

        StandingRow alpha = standingsService.getStandings(season).stream()
                .filter(r -> r.getTeamId().equals(home.getTeamId())).findFirst().orElseThrow();
        assertThat(alpha.getPointsDeducted()).isEqualTo(3);
        assertThat(alpha.getPoints()).isEqualTo(0);
    }
}
