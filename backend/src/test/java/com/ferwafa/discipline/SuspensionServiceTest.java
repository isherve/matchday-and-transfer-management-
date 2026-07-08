package com.ferwafa.discipline;

import com.ferwafa.common.*;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.member.TeamMember;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.referee.Referee;
import com.ferwafa.referee.RefereeRepository;
import com.ferwafa.report.MatchReport;
import com.ferwafa.report.MatchReportRepository;
import com.ferwafa.fixture.LineupRepository;
import com.ferwafa.transfer.TransferRepository;
import com.ferwafa.team.Team;
import com.ferwafa.team.TeamRepository;
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
class SuspensionServiceTest {

    @Autowired private SuspensionService suspensionService;
    @Autowired private TeamRepository teamRepository;
    @Autowired private FixtureRepository fixtureRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private RefereeRepository refereeRepository;
    @Autowired private DisciplinaryRecordRepository disciplinaryRecordRepository;

    private Team team;
    private TeamMember player;
    private Fixture fixtureWeek1;
    private Fixture fixtureWeek2;
    private Referee referee;

    @BeforeEach
    void setUp() {
        team = teamRepository.save(Team.builder().name("Test FC").username("testfc_" + System.nanoTime())
                .passwordHash("hash").stadium("Test Stadium").build());
        player = teamMemberRepository.save(TeamMember.builder().fname("John").lname("Doe")
                .number(9).roleInTeam(MemberRole.PLAYER).position("FW").team(team).build());
        referee = refereeRepository.save(Referee.builder().fname("Ref").lname("One")
                .email("ref_" + System.nanoTime() + "@test.com").accessCodeHash("hash").build());

        fixtureWeek1 = fixtureRepository.save(Fixture.builder()
                .homeTeam(team).awayTeam(team).week(1).season("2025/26")
                .matchDate(LocalDate.now()).matchTime(LocalTime.NOON)
                .status(FixtureStatus.APPROVED).referee(referee).build());

        fixtureWeek2 = fixtureRepository.save(Fixture.builder()
                .homeTeam(team).awayTeam(team).week(2).season("2025/26")
                .matchDate(LocalDate.now().plusDays(7)).matchTime(LocalTime.NOON)
                .status(FixtureStatus.SCHEDULED).referee(referee).build());
    }

    @Test
    void redCardSuspendsPlayerForNextMatch() {
        disciplinaryRecordRepository.save(DisciplinaryRecord.builder()
                .teamMember(player).fixture(fixtureWeek1).week(1)
                .cardType(CardType.RED).cardMin(80)
                .suspensionReason(SuspensionReason.RED_CARD)
                .suspensionFixture(fixtureWeek2).suspensionServed(false).build());

        var suspensions = suspensionService.getSuspendedPlayers(team.getTeamId(), fixtureWeek2.getId());
        assertThat(suspensions).hasSize(1);
        assertThat(suspensions.get(0).getReason()).isEqualTo(SuspensionReason.RED_CARD);
        assertThat(suspensionService.isPlayerSuspended(player.getMemberId(), fixtureWeek2.getId())).isTrue();
    }

    @Test
    void twoYellowCardsSuspendPlayer() {
        disciplinaryRecordRepository.save(DisciplinaryRecord.builder()
                .teamMember(player).fixture(fixtureWeek1).week(1)
                .cardType(CardType.YELLOW).cardMin(30)
                .suspensionReason(SuspensionReason.TWO_YELLOWS)
                .suspensionFixture(fixtureWeek2).suspensionServed(false).build());

        var suspensions = suspensionService.getSuspendedPlayers(team.getTeamId(), fixtureWeek2.getId());
        assertThat(suspensions).hasSize(1);
        assertThat(suspensions.get(0).getReason()).isEqualTo(SuspensionReason.TWO_YELLOWS);
    }

    @Test
    void eligiblePlayerNotSuspended() {
        var suspensions = suspensionService.getSuspendedPlayers(team.getTeamId(), fixtureWeek2.getId());
        assertThat(suspensions).isEmpty();
    }
}
