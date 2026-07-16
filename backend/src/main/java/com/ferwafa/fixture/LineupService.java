package com.ferwafa.fixture;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.MemberRole;
import com.ferwafa.config.LeagueRuleService;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.discipline.SuspensionService;
import com.ferwafa.discipline.dto.SuspensionDto;
import com.ferwafa.fixture.dto.LineupRequest;
import com.ferwafa.fixture.dto.LineupResponse;
import com.ferwafa.member.MemberService;
import com.ferwafa.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LineupService {

    private final LineupRepository lineupRepository;
    private final FixtureService fixtureService;
    private final TeamService teamService;
    private final MemberService memberService;
    private final SuspensionService suspensionService;
    private final SecurityUtils securityUtils;
    private final LeagueRuleService leagueRuleService;

    @Transactional(readOnly = true)
    public List<LineupResponse> getLineup(Long fixtureId, Long teamId) {
        // Referees need read access to review starting XIs on matchday
        teamService.assertTeamReadAccess(teamId);
        List<SuspensionDto> suspensions = suspensionService.getSuspendedPlayers(teamId, fixtureId);
        Map<Long, SuspensionDto> suspensionMap = suspensions.stream()
                .collect(Collectors.toMap(SuspensionDto::getMemberId, s -> s));

        return lineupRepository.findByFixtureIdAndTeamTeamId(fixtureId, teamId).stream()
                .map(lineup -> {
                    SuspensionDto susp = suspensionMap.get(lineup.getMember().getMemberId());
                    return LineupResponse.builder()
                            .id(lineup.getId())
                            .fixtureId(fixtureId)
                            .teamId(teamId)
                            .memberId(lineup.getMember().getMemberId())
                            .playerName(lineup.getMember().getFname() + " " + lineup.getMember().getLname())
                            .playerNumber(lineup.getMember().getNumber())
                            .position(lineup.getMember().getPosition())
                            .suspended(susp != null)
                            .suspensionReason(susp != null ? susp.getReasonLabel() : null)
                            .build();
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LineupResponse> getLineupWithEligibility(Long fixtureId, Long teamId) {
        teamService.assertTeamReadAccess(teamId);
        List<SuspensionDto> suspensions = suspensionService.getSuspendedPlayers(teamId, fixtureId);
        Map<Long, SuspensionDto> suspensionMap = suspensions.stream()
                .collect(Collectors.toMap(SuspensionDto::getMemberId, s -> s));

        return memberService.findByTeam(teamId).stream()
                .filter(m -> m.getRoleInTeam() == MemberRole.PLAYER)
                .map(m -> {
                    SuspensionDto susp = suspensionMap.get(m.getMemberId());
                    boolean inLineup = lineupRepository.existsByFixtureIdAndTeamTeamIdAndMemberMemberId(
                            fixtureId, teamId, m.getMemberId());
                    return LineupResponse.builder()
                            .fixtureId(fixtureId)
                            .teamId(teamId)
                            .memberId(m.getMemberId())
                            .playerName(m.getFname() + " " + m.getLname())
                            .playerNumber(m.getNumber())
                            .position(m.getPosition())
                            .suspended(susp != null)
                            .suspensionReason(susp != null ? susp.getReasonLabel() : null)
                            .build();
                }).collect(Collectors.toList());
    }

    @Transactional
    public List<LineupResponse> saveLineup(Long fixtureId, LineupRequest request) {
        teamService.assertTeamAccess(request.getTeamId());
        fixtureService.getFixture(fixtureId);
        var team = teamService.getTeam(request.getTeamId());
        leagueRuleService.assertLineupSize(request.getMemberIds().size());

        List<SuspensionDto> suspensions = suspensionService.getSuspendedPlayers(request.getTeamId(), fixtureId);
        for (Long memberId : request.getMemberIds()) {
            boolean suspended = suspensions.stream().anyMatch(s -> s.getMemberId().equals(memberId));
            if (suspended) {
                var member = memberService.getMember(memberId);
                throw new BusinessException(
                        "Player " + member.getFname() + " " + member.getLname() +
                                " is suspended and cannot be included in the lineup",
                        HttpStatus.BAD_REQUEST);
            }
        }

        lineupRepository.findByFixtureIdAndTeamTeamId(fixtureId, request.getTeamId())
                .forEach(lineupRepository::delete);

        List<Lineup> saved = new ArrayList<>();
        for (Long memberId : request.getMemberIds()) {
            var member = memberService.getMember(memberId);
            if (!member.getTeam().getTeamId().equals(request.getTeamId())) {
                throw new BusinessException("Player does not belong to this team");
            }
            Lineup lineup = Lineup.builder()
                    .fixture(fixtureService.getFixture(fixtureId))
                    .team(team)
                    .member(member)
                    .build();
            saved.add(lineupRepository.save(lineup));
        }

        suspensionService.markSuspensionsServed(fixtureId, request.getTeamId());

        return saved.stream().map(l -> LineupResponse.builder()
                .id(l.getId())
                .fixtureId(fixtureId)
                .teamId(request.getTeamId())
                .memberId(l.getMember().getMemberId())
                .playerName(l.getMember().getFname() + " " + l.getMember().getLname())
                .playerNumber(l.getMember().getNumber())
                .position(l.getMember().getPosition())
                .suspended(false)
                .build()).collect(Collectors.toList());
    }
}
