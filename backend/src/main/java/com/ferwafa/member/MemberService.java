package com.ferwafa.member;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.MemberRole;
import com.ferwafa.config.LeagueRuleService;
import com.ferwafa.member.dto.MemberRequest;
import com.ferwafa.member.dto.MemberResponse;
import com.ferwafa.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final TeamMemberRepository memberRepository;
    private final TeamService teamService;
    private final LeagueRuleService leagueRuleService;

    @Transactional(readOnly = true)
    public List<MemberResponse> findByTeam(Long teamId) {
        teamService.assertTeamReadAccess(teamId);
        return memberRepository.findByTeamTeamId(teamId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse create(Long teamId, MemberRequest request) {
        teamService.assertTeamAccess(teamId);
        var team = teamService.getTeam(teamId);
        if (request.getRoleInTeam() == MemberRole.PLAYER) {
            long players = memberRepository.findByTeamTeamIdAndRoleInTeam(teamId, MemberRole.PLAYER).size();
            leagueRuleService.assertSquadSizeAllowsNewPlayer(players);
        }
        TeamMember member = TeamMember.builder()
                .fname(request.getFname())
                .lname(request.getLname())
                .number(request.getNumber())
                .roleInTeam(request.getRoleInTeam())
                .post(request.getPost())
                .position(request.getPosition())
                .contract(request.getContract())
                .team(team)
                .build();
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        TeamMember member = getMember(id);
        teamService.assertTeamAccess(member.getTeam().getTeamId());
        member.setFname(request.getFname());
        member.setLname(request.getLname());
        member.setNumber(request.getNumber());
        member.setRoleInTeam(request.getRoleInTeam());
        member.setPost(request.getPost());
        member.setPosition(request.getPosition());
        member.setContract(request.getContract());
        return toResponse(memberRepository.save(member));
    }

    @Transactional
    public void delete(Long id) {
        TeamMember member = getMember(id);
        teamService.assertTeamAccess(member.getTeam().getTeamId());
        memberRepository.delete(member);
    }

    public TeamMember getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Member not found", HttpStatus.NOT_FOUND));
    }

    private MemberResponse toResponse(TeamMember member) {
        return MemberResponse.builder()
                .memberId(member.getMemberId())
                .fname(member.getFname())
                .lname(member.getLname())
                .number(member.getNumber())
                .roleInTeam(member.getRoleInTeam())
                .post(member.getPost())
                .position(member.getPosition())
                .contract(member.getContract())
                .teamId(member.getTeam().getTeamId())
                .teamName(member.getTeam().getName())
                .build();
    }
}
