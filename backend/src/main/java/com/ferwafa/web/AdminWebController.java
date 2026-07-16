package com.ferwafa.web;

import com.ferwafa.fixture.FixtureService;
import com.ferwafa.referee.RefereeService;
import com.ferwafa.report.MatchReportService;
import com.ferwafa.report.ReportExportService;
import com.ferwafa.standings.StandingsService;
import com.ferwafa.team.TeamService;
import com.ferwafa.transfer.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final TeamService teamService;
    private final FixtureService fixtureService;
    private final MatchReportService matchReportService;
    private final TransferService transferService;
    private final ReportExportService reportExportService;
    private final StandingsService standingsService;
    private final RefereeService refereeService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("referees", refereeService.findAll());
        model.addAttribute("fixtures", fixtureService.findAll());
        model.addAttribute("upcomingFixtures", fixtureService.findAll().stream()
                .filter(f -> f.getStatus() == com.ferwafa.common.FixtureStatus.SCHEDULED
                        || f.getStatus() == com.ferwafa.common.FixtureStatus.REFEREE_ASSIGNED
                        || f.getStatus() == com.ferwafa.common.FixtureStatus.POSTPONED)
                .toList());
        model.addAttribute("pendingReports", matchReportService.getPendingReports());
        model.addAttribute("transfers", transferService.findAll());
        model.addAttribute("punishments", reportExportService.getPunishmentsReport());
        model.addAttribute("activeSuspensions", reportExportService.getActiveSuspensionsReport());
        return "admin/dashboard";
    }

    @GetMapping("/referees")
    public String referees(Model model) {
        model.addAttribute("referees", refereeService.findAll());
        model.addAttribute("fixtures", fixtureService.findAll());
        model.addAttribute("teams", teamService.findAll());
        return "admin/referees";
    }

    @GetMapping("/teams")
    public String teams(Model model) {
        model.addAttribute("teams", teamService.findAll());
        return "admin/teams";
    }

    @GetMapping("/teams/new")
    public String newTeam() {
        return "admin/team-form";
    }

    @GetMapping("/fixtures")
    public String fixtures(Model model) {
        model.addAttribute("fixtures", fixtureService.findAll());
        model.addAttribute("teams", teamService.findAll());
        model.addAttribute("referees", refereeService.findAll());
        return "admin/fixtures";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("pendingReports", matchReportService.getPendingReports());
        model.addAttribute("approvedReports", matchReportService.getApprovedReports());
        return "admin/reports";
    }

    @GetMapping("/reports/submitted")
    public String submittedReports(Model model) {
        model.addAttribute("pendingReports", matchReportService.getPendingReports());
        model.addAttribute("title", "Submitted Match Reports");
        return "admin/report-submitted";
    }

    @GetMapping("/reports/match/{fixtureId}")
    public String matchReportDetail(@PathVariable Long fixtureId, Model model) {
        model.addAttribute("fixture", fixtureService.findById(fixtureId));
        model.addAttribute("entries", matchReportService.getReports(fixtureId));
        model.addAttribute("comments", matchReportService.getComments(fixtureId));
        model.addAttribute("editLogs", matchReportService.getEditLogs(fixtureId));
        return "admin/report-detail";
    }

    @GetMapping("/reports/fixtures")
    public String fixturesReport(Model model) {
        model.addAttribute("data", reportExportService.getFixturesReport());
        model.addAttribute("title", "Fixtures Report");
        model.addAttribute("activeNav", "season");
        model.addAttribute("pdfUrl", "/api/reports/fixtures/pdf");
        return "admin/report-table";
    }

    @GetMapping("/reports/punishments")
    public String punishmentsReport(Model model) {
        model.addAttribute("data", reportExportService.getPunishmentsReport());
        model.addAttribute("title", "Punishments Report");
        model.addAttribute("activeNav", "punishments");
        model.addAttribute("pdfUrl", "/api/reports/punishments/pdf");
        return "admin/report-table";
    }

    @GetMapping("/transfers")
    public String transfersManage(Model model) {
        model.addAttribute("transfers", transferService.findAll());
        return "admin/transfers";
    }

    @GetMapping("/reports/transfers")
    public String transfersReport(Model model) {
        model.addAttribute("data", reportExportService.getTransfersReport());
        model.addAttribute("title", "Transfers Report");
        model.addAttribute("activeNav", "transfers");
        model.addAttribute("pdfUrl", "/api/reports/transfers/pdf");
        return "admin/report-table";
    }

    @GetMapping("/reports/season")
    public String seasonReport(@RequestParam(defaultValue = "2025/26") String season, Model model) {
        model.addAttribute("standings", standingsService.getStandings(season));
        model.addAttribute("season", season);
        model.addAttribute("topScorers", standingsService.getTopScorers(season, 10));
        model.addAttribute("cards", standingsService.getCardsLeaderboard(season, 10));
        return "admin/report-season";
    }
}
