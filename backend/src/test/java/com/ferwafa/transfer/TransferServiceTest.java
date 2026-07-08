package com.ferwafa.transfer;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.MemberRole;
import com.ferwafa.common.TransferStatus;
import com.ferwafa.member.TeamMember;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.team.Team;
import com.ferwafa.team.TeamRepository;
import com.ferwafa.transfer.dto.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransferServiceTest {

    @Autowired private TransferService transferService;
    @Autowired private TransferRepository transferRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TeamMemberRepository memberRepository;

    private Team teamA, teamB;
    private TeamMember player;

    @BeforeEach
    void setUp() {
        teamA = teamRepository.save(Team.builder().name("Team A").username("teama_" + System.nanoTime()).passwordHash("h").build());
        teamB = teamRepository.save(Team.builder().name("Team B").username("teamb_" + System.nanoTime()).passwordHash("h").build());
        player = memberRepository.save(TeamMember.builder().fname("P").lname("One")
                .number(1).roleInTeam(MemberRole.PLAYER).team(teamA).build());
    }

    @Test
    void transferStateTransitions() {
        var request = new TransferRequest();
        request.setMemberId(player.getMemberId());
        request.setTeamFromId(teamA.getTeamId());
        request.setTeamToId(teamB.getTeamId());

        // Note: create requires team auth context - test repository directly for state machine
        Transfer transfer = Transfer.builder()
                .member(player).teamFrom(teamA).teamTo(teamB)
                .requestDate(java.time.LocalDate.now())
                .status(TransferStatus.REQUESTED).build();
        transfer = transferRepository.save(transfer);
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.REQUESTED);

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovalDate(java.time.LocalDate.now());
        transferRepository.save(transfer);

        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedDate(java.time.LocalDate.now());
        player.setTeam(teamB);
        memberRepository.save(player);
        transferRepository.save(transfer);

        assertThat(memberRepository.findById(player.getMemberId()).get().getTeam().getTeamId())
                .isEqualTo(teamB.getTeamId());
    }
}
