package com.ferwafa.web;

import com.ferwafa.config.SecurityUtils;
import com.ferwafa.discipline.SuspensionService;
import com.ferwafa.fixture.FixtureService;
import com.ferwafa.fixture.LineupService;
import com.ferwafa.fixture.dto.FixtureResponse;
import com.ferwafa.member.MemberService;
import com.ferwafa.team.TeamService;
import com.ferwafa.transfer.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamWebController {

    private final SecurityUtils securityUtils;
    private final TeamService teamService;
    private final MemberService memberService;
    private final FixtureService fixtureService;
    private final LineupService lineupService;
    private final SuspensionService suspensionService;
    private final TransferService transferService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long teamId = securityUtils.currentEntityId();
        model.addAttribute("team", teamService.findById(teamId));
        model.addAttribute("members", memberService.findByTeam(teamId));
        model.addAttribute("fixtures", fixtureService.findByTeamAndSeason(teamId, "2025/26"));
        return "team/dashboard";
    }

    @GetMapping("/members")
    public String members(Model model) {
        Long teamId = securityUtils.currentEntityId();
        model.addAttribute("team", teamService.findById(teamId));
        model.addAttribute("members", memberService.findByTeam(teamId));
        model.addAttribute("suspendedIds", suspensionService.getActiveSuspendedMemberIds(teamId));
        return "team/members";
    }

    @GetMapping("/members/new")
    public String newMember(Model model) {
        model.addAttribute("team", teamService.findById(securityUtils.currentEntityId()));
        return "team/member-form";
    }

    @GetMapping("/lineup")
    public String lineup(@RequestParam(required = false) Long fixtureId, Model model) {
        Long teamId = securityUtils.currentEntityId();
        model.addAttribute("team", teamService.findById(teamId));

        List<FixtureResponse> fixtures = fixtureService.findByTeamAndSeason(teamId, "2025/26");

        if (fixtureId == null && !fixtures.isEmpty()) {
            fixtureId = fixtures.stream()
                    .filter(f -> f.getStatus().name().equals("SCHEDULED")
                            || f.getStatus().name().equals("REFEREE_ASSIGNED")
                            || f.getStatus().name().equals("POSTPONED"))
                    .findFirst()
                    .map(FixtureResponse::getId)
                    .orElse(fixtures.get(fixtures.size() - 1).getId());
        }

        model.addAttribute("fixtures", fixtures);
        model.addAttribute("fixtureId", fixtureId);
        if (fixtureId != null) {
            model.addAttribute("players", lineupService.getLineupWithEligibility(fixtureId, teamId));
            model.addAttribute("lineup", lineupService.getLineup(fixtureId, teamId));
            model.addAttribute("suspensions", suspensionService.getSuspendedPlayers(teamId, fixtureId));
        }
        return "team/lineup";
    }

    @GetMapping("/transfers")
    public String transfers(Model model) {
        Long teamId = securityUtils.currentEntityId();
        model.addAttribute("team", teamService.findById(teamId));
        model.addAttribute("transfers", transferService.findAll());
        model.addAttribute("members", memberService.findByTeam(teamId));
        model.addAttribute("teams", teamService.findAll());
        return "team/transfers";
    }
}
