package com.ferwafa.report;

import com.ferwafa.standings.StandingsService;
import com.ferwafa.standings.dto.LeaderboardEntry;
import com.ferwafa.standings.dto.StandingRow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportExportController {

    private final ReportExportService reportExportService;
    private final StandingsService standingsService;

    @GetMapping("/fixtures")
    @Operation(summary = "Fixtures report")
    public ResponseEntity<List<Map<String, Object>>> fixtures() {
        return ResponseEntity.ok(reportExportService.getFixturesReport());
    }

    @GetMapping("/fixtures/pdf")
    @Operation(summary = "Fixtures report PDF")
    public ResponseEntity<byte[]> fixturesPdf() {
        return pdfResponse("FERWAFA Fixtures Report",
                List.of("Week", "Match", "Date", "Time", "Stadium", "Season", "Status", "Referee"),
                reportExportService.getFixturesReport().stream()
                        .map(r -> List.of(
                                String.valueOf(r.get("week")),
                                String.valueOf(r.get("match")),
                                String.valueOf(r.get("date")),
                                String.valueOf(r.get("time")),
                                String.valueOf(r.get("stadium")),
                                String.valueOf(r.get("season")),
                                String.valueOf(r.get("status")),
                                String.valueOf(r.get("referee"))
                        )).toList());
    }

    @GetMapping("/punishments")
    @Operation(summary = "Punishments report")
    public ResponseEntity<List<Map<String, Object>>> punishments() {
        return ResponseEntity.ok(reportExportService.getPunishmentsReport());
    }

    @GetMapping("/punishments/pdf")
    @Operation(summary = "Punishments report PDF")
    public ResponseEntity<byte[]> punishmentsPdf() {
        return pdfResponse("FERWAFA Punishments Report",
                List.of("Player", "Team", "Week", "Card", "Minute", "Suspension", "Served"),
                reportExportService.getPunishmentsReport().stream()
                        .map(r -> List.of(
                                String.valueOf(r.get("player")),
                                String.valueOf(r.get("team")),
                                String.valueOf(r.get("week")),
                                String.valueOf(r.get("card")),
                                String.valueOf(r.get("minute")),
                                String.valueOf(r.get("suspension")),
                                String.valueOf(r.get("served"))
                        )).toList());
    }

    @GetMapping("/transfers")
    @Operation(summary = "Transfers report")
    public ResponseEntity<List<Map<String, Object>>> transfers() {
        return ResponseEntity.ok(reportExportService.getTransfersReport());
    }

    @GetMapping("/transfers/pdf")
    @Operation(summary = "Transfers report PDF")
    public ResponseEntity<byte[]> transfersPdf() {
        return pdfResponse("FERWAFA Transfers Report",
                List.of("Player", "From", "To", "Request Date", "Status", "Approval", "Completed"),
                reportExportService.getTransfersReport().stream()
                        .map(r -> List.of(
                                String.valueOf(r.get("player")),
                                String.valueOf(r.get("from")),
                                String.valueOf(r.get("to")),
                                String.valueOf(r.get("requestDate")),
                                String.valueOf(r.get("status")),
                                String.valueOf(r.get("approvalDate")),
                                String.valueOf(r.get("completedDate"))
                        )).toList());
    }

    @GetMapping("/season")
    @Operation(summary = "Season record / league table (legacy alias)")
    public ResponseEntity<List<StandingRow>> season(@RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(standingsService.getStandings(season));
    }

    @GetMapping("/standings")
    @Operation(summary = "League standings with points, GD, and club sanctions applied")
    public ResponseEntity<List<StandingRow>> standings(@RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(standingsService.getStandings(season));
    }

    @GetMapping("/standings/pdf")
    @Operation(summary = "League standings PDF")
    public ResponseEntity<byte[]> standingsPdf(@RequestParam(defaultValue = "2025/26") String season) {
        return pdfResponse("FERWAFA League Standings " + season,
                List.of("Pos", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts", "Ded"),
                standingsService.getStandings(season).stream()
                        .map(r -> List.of(
                                String.valueOf(r.getPosition()),
                                r.getTeamName(),
                                String.valueOf(r.getPlayed()),
                                String.valueOf(r.getWon()),
                                String.valueOf(r.getDrawn()),
                                String.valueOf(r.getLost()),
                                String.valueOf(r.getGoalsFor()),
                                String.valueOf(r.getGoalsAgainst()),
                                String.valueOf(r.getGoalDifference()),
                                String.valueOf(r.getPoints()),
                                String.valueOf(r.getPointsDeducted())
                        )).toList());
    }

    @GetMapping("/top-scorers")
    @Operation(summary = "Top scorers leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> topScorers(
            @RequestParam(defaultValue = "2025/26") String season,
            @RequestParam(defaultValue = "20") int limit) {
        List<LeaderboardEntry> entries = standingsService.getTopScorers(season, limit);
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/cards-leaderboard")
    @Operation(summary = "Cards leaderboard (red then yellow)")
    public ResponseEntity<List<LeaderboardEntry>> cardsLeaderboard(
            @RequestParam(defaultValue = "2025/26") String season,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(standingsService.getCardsLeaderboard(season, limit));
    }

    @GetMapping("/season/pdf")
    @Operation(summary = "Season record PDF")
    public ResponseEntity<byte[]> seasonPdf(@RequestParam(defaultValue = "2025/26") String season) {
        return standingsPdf(season);
    }

    private ResponseEntity<byte[]> pdfResponse(String title, List<String> headers, List<List<String>> rows) {
        byte[] pdf = reportExportService.generatePdf(title, headers, new ArrayList<>(rows));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
