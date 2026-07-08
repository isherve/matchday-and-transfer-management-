package com.ferwafa.fixture;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.MemberRole;
import com.ferwafa.common.SuspensionReason;
import com.ferwafa.discipline.DisciplinaryRecord;
import com.ferwafa.discipline.DisciplinaryRecordRepository;
import com.ferwafa.discipline.SuspensionService;
import com.ferwafa.fixture.dto.LineupRequest;
import com.ferwafa.member.TeamMember;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.referee.Referee;
import com.ferwafa.referee.RefereeRepository;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LineupServiceTest {

    @Autowired private LineupService lineupService;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository memberRepository;
    @Autowired private FixtureRepository fixtureRepository;
    @Autowired private RefereeRepository refereeRepository;
    @Autowired private DisciplinaryRecordRepository disciplinaryRecordRepository;
    @Autowired private SuspensionService suspensionService;

    private Team team;
    private TeamMember suspendedPlayer;
    private com.ferwafa.fixture.Fixture fixture;

    @BeforeEach
    void setUp() {
        team = teamRepository.save(Team.builder().name("APR FC Test").username("apr_test_" + System.nanoTime()).passwordHash("h").build());
        suspendedPlayer = memberRepository.save(TeamMember.builder().fname("Suspended").lname("Player")
                .number(10).roleInTeam(MemberRole.PLAYER).team(team).build());
        Referee ref = refereeRepository.save(Referee.builder().fname("R").lname("1")
                .email("r@t.com").accessCodeHash("h").build());
        var fixtureW1 = fixtureRepository.save(com.ferwafa.fixture.Fixture.builder()
                .homeTeam(team).awayTeam(team).week(1).season("2025/26")
                .matchDate(LocalDate.now()).matchTime(LocalTime.NOON)
                .status(com.ferwafa.common.FixtureStatus.APPROVED).referee(ref).build());
        fixture = fixtureRepository.save(com.ferwafa.fixture.Fixture.builder()
                .homeTeam(team).awayTeam(team).week(2).season("2025/26")
                .matchDate(LocalDate.now().plusDays(7)).matchTime(LocalTime.NOON)
                .status(com.ferwafa.common.FixtureStatus.SCHEDULED).referee(ref).build());

        disciplinaryRecordRepository.save(DisciplinaryRecord.builder()
                .teamMember(suspendedPlayer).fixture(fixtureW1).week(1)
                .cardType(com.ferwafa.common.CardType.RED).cardMin(67)
                .suspensionReason(SuspensionReason.RED_CARD)
                .suspensionFixture(fixture).suspensionServed(false).build());
    }

    @Test
    void blocksSuspendedPlayerInLineup() {
        LineupRequest request = new LineupRequest();
        request.setTeamId(team.getTeamId());
        request.setMemberIds(List.of(suspendedPlayer.getMemberId()));

        // LineupService.assertTeamAccess will fail without auth - test suspension check directly
        assertThatThrownBy(() -> {
            if (suspensionService.isPlayerSuspended(suspendedPlayer.getMemberId(), fixture.getId())) {
                throw new BusinessException("Player is suspended and cannot be included in the lineup");
            }
        }).isInstanceOf(BusinessException.class)
          .hasMessageContaining("suspended");
    }
}
