package com.ferwafa.report;

import com.ferwafa.common.FixtureStatus;
import com.ferwafa.discipline.SuspensionService;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.report.dto.MatchReportResponse;
import com.ferwafa.transfer.TransferRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final FixtureRepository fixtureRepository;
    private final SuspensionService suspensionService;
    private final TransferRepository transferRepository;
    private final MatchReportService matchReportService;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFixturesReport() {
        return fixtureRepository.findAll().stream().map(f -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", f.getId());
            row.put("week", f.getWeek());
            row.put("match", f.getHomeTeam().getName() + " vs " + f.getAwayTeam().getName());
            row.put("date", f.getMatchDate().toString());
            row.put("time", f.getMatchTime().toString());
            row.put("stadium", f.getStadium());
            row.put("season", f.getSeason());
            row.put("status", f.getStatus().name());
            row.put("referee", f.getReferee() != null ?
                    f.getReferee().getFname() + " " + f.getReferee().getLname() : "Unassigned");
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPunishmentsReport() {
        return suspensionService.getAllPunishments().stream().map(d -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("player", d.getTeamMember().getFname() + " " + d.getTeamMember().getLname());
            row.put("team", d.getTeamMember().getTeam().getName());
            row.put("week", d.getWeek());
            row.put("card", d.getCardType().name());
            row.put("minute", d.getCardMin());
            row.put("suspension", d.getSuspensionReason() != null ? d.getSuspensionReason().name() : "N/A");
            row.put("served", d.getSuspensionServed());
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTransfersReport() {
        return transferRepository.findAll().stream().map(t -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("player", t.getMember().getFname() + " " + t.getMember().getLname());
            row.put("from", t.getTeamFrom().getName());
            row.put("to", t.getTeamTo().getName());
            row.put("requestDate", t.getRequestDate().toString());
            row.put("status", t.getStatus().name());
            row.put("approvalDate", t.getApprovalDate() != null ? t.getApprovalDate().toString() : "");
            row.put("completedDate", t.getCompletedDate() != null ? t.getCompletedDate().toString() : "");
            return row;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSeasonRecord(String season) {
        List<Fixture> fixtures = fixtureRepository.findBySeasonOrderByWeekAscMatchDateAsc(season);
        Map<Long, TeamStats> stats = new HashMap<>();

        for (Fixture f : fixtures) {
            initStats(stats, f.getHomeTeam().getTeamId(), f.getHomeTeam().getName());
            initStats(stats, f.getAwayTeam().getTeamId(), f.getAwayTeam().getName());
        }

        for (Fixture f : fixtures) {
            if (f.getStatus() != FixtureStatus.APPROVED) continue;

            int homeGoals = matchReportService.getReports(f.getId()).stream()
                    .filter(r -> isMemberOfTeam(r.getTeamMemberId(), f.getHomeTeam().getTeamId()))
                    .mapToInt(MatchReportResponse::getGoal).sum();
            int awayGoals = matchReportService.getReports(f.getId()).stream()
                    .filter(r -> isMemberOfTeam(r.getTeamMemberId(), f.getAwayTeam().getTeamId()))
                    .mapToInt(MatchReportResponse::getGoal).sum();

            TeamStats home = stats.get(f.getHomeTeam().getTeamId());
            TeamStats away = stats.get(f.getAwayTeam().getTeamId());
            home.played++;
            away.played++;
            home.goalsFor += homeGoals;
            home.goalsAgainst += awayGoals;
            away.goalsFor += awayGoals;
            away.goalsAgainst += homeGoals;
            if (homeGoals > awayGoals) { home.won++; away.lost++; }
            else if (homeGoals < awayGoals) { away.won++; home.lost++; }
            else { home.drawn++; away.drawn++; }
            home.points = home.won * 3 + home.drawn;
            away.points = away.won * 3 + away.drawn;
        }

        return stats.values().stream()
                .sorted(Comparator.comparingInt((TeamStats s) -> s.points).reversed()
                        .thenComparingInt(s -> s.goalsFor - s.goalsAgainst).reversed())
                .map(s -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("team", s.name);
                    row.put("played", s.played);
                    row.put("won", s.won);
                    row.put("drawn", s.drawn);
                    row.put("lost", s.lost);
                    row.put("goalsFor", s.goalsFor);
                    row.put("goalsAgainst", s.goalsAgainst);
                    row.put("goalDifference", s.goalsFor - s.goalsAgainst);
                    row.put("points", s.points);
                    return row;
                }).collect(Collectors.toList());
    }

    private boolean isMemberOfTeam(Long memberId, Long teamId) {
        return teamMemberRepository.findById(memberId)
                .map(m -> m.getTeam().getTeamId().equals(teamId))
                .orElse(false);
    }

    private void initStats(Map<Long, TeamStats> stats, Long teamId, String name) {
        stats.putIfAbsent(teamId, new TeamStats(name));
    }

    public byte[] generatePdf(String title, List<String> headers, List<List<String>> rows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);

            Paragraph datePara = new Paragraph("Generated: " +
                    java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), cellFont);
            datePara.setSpacingAfter(15);
            document.add(datePara);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new java.awt.Color(0, 102, 51));
                table.addCell(cell);
            }

            for (List<String> row : rows) {
                for (String value : row) {
                    table.addCell(new Phrase(value != null ? value : "", cellFont));
                }
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new com.ferwafa.common.BusinessException("Failed to generate PDF: " + e.getMessage());
        }
    }

    private static class TeamStats {
        String name;
        int played, won, drawn, lost, goalsFor, goalsAgainst, points;
        TeamStats(String name) { this.name = name; }
    }
}
