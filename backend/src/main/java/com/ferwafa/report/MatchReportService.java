package com.ferwafa.report;

import com.ferwafa.auth.AuthService;
import com.ferwafa.common.BusinessException;
import com.ferwafa.common.FixtureStatus;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.ReportStatus;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.discipline.SuspensionService;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.fixture.FixtureService;
import com.ferwafa.member.MemberService;
import com.ferwafa.member.TeamMember;
import com.ferwafa.notification.NotificationService;
import com.ferwafa.referee.Referee;
import com.ferwafa.referee.RefereeService;
import com.ferwafa.report.dto.MatchReportCommentRequest;
import com.ferwafa.report.dto.MatchReportCommentResponse;
import com.ferwafa.report.dto.MatchReportEditLogResponse;
import com.ferwafa.report.dto.MatchReportEntryRequest;
import com.ferwafa.report.dto.MatchReportResponse;
import com.ferwafa.report.dto.MatchReportSubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchReportService {

    private final MatchReportRepository matchReportRepository;
    private final MatchReportCommentRepository commentRepository;
    private final MatchReportEditLogRepository editLogRepository;
    private final FixtureRepository fixtureRepository;
    private final FixtureService fixtureService;
    private final MemberService memberService;
    private final RefereeService refereeService;
    private final SuspensionService suspensionService;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<MatchReportResponse> getReports(Long fixtureId) {
        return matchReportRepository.findByFixtureId(fixtureId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<MatchReportResponse> submitReport(Long fixtureId, MatchReportSubmitRequest request) {
        if (!securityUtils.isReferee()) {
            throw new BusinessException("Only referees can submit match reports", HttpStatus.FORBIDDEN);
        }
        return upsertReport(fixtureId, request, "SUBMITTED", true);
    }

    @Transactional
    public List<MatchReportResponse> adminUpdateReport(Long fixtureId, MatchReportSubmitRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException("Only admin can edit match reports", HttpStatus.FORBIDDEN);
        }
        return upsertReport(fixtureId, request, "UPDATED", false);
    }

    private List<MatchReportResponse> upsertReport(Long fixtureId, MatchReportSubmitRequest request,
                                                   String action, boolean notifyAdmin) {
        Fixture fixture = fixtureService.getFixture(fixtureId);
        boolean admin = securityUtils.isAdmin();
        boolean referee = securityUtils.isReferee();

        if (!admin && !referee) {
            throw new BusinessException("Not allowed to edit this report", HttpStatus.FORBIDDEN);
        }

        if (referee) {
            Long refereeId = securityUtils.currentEntityId();
            if (fixture.getReferee() == null || !fixture.getReferee().getRefereeId().equals(refereeId)) {
                throw new BusinessException("You are not assigned to this fixture", HttpStatus.FORBIDDEN);
            }
            if (fixture.getStatus() == FixtureStatus.APPROVED) {
                throw new BusinessException("Report already approved and cannot be modified");
            }
        }

        Referee reportReferee = fixture.getReferee();
        if (reportReferee == null && referee) {
            reportReferee = refereeService.getReferee(securityUtils.currentEntityId());
        }
        if (reportReferee == null) {
            throw new BusinessException("Assign a referee before editing the match report");
        }

        boolean wasApproved = fixture.getStatus() == FixtureStatus.APPROVED;
        ReportStatus entryStatus = wasApproved ? ReportStatus.APPROVED : ReportStatus.SUBMITTED;

        matchReportRepository.findByFixtureId(fixtureId).forEach(matchReportRepository::delete);

        List<MatchReport> saved = new ArrayList<>();
        int homeGoals = 0;
        int awayGoals = 0;
        Long homeId = fixture.getHomeTeam().getTeamId();
        Long awayId = fixture.getAwayTeam().getTeamId();

        for (MatchReportEntryRequest entry : request.getEntries()) {
            if ((entry.getGoal() == null || entry.getGoal() == 0)
                    && (entry.getCard() == null || entry.getCard() == com.ferwafa.common.CardType.NONE)) {
                continue;
            }
            var member = memberService.getMember(entry.getTeamMemberId());
            int goals = entry.getGoal() != null ? entry.getGoal() : 0;
            MatchReport report = MatchReport.builder()
                    .fixture(fixture)
                    .teamMember(member)
                    .goal(goals)
                    .goalMin(entry.getGoalMin())
                    .card(entry.getCard() != null ? entry.getCard() : com.ferwafa.common.CardType.NONE)
                    .cardMin(entry.getCardMin())
                    .week(fixture.getWeek())
                    .status(entryStatus)
                    .submittedByReferee(reportReferee)
                    .build();
            saved.add(matchReportRepository.save(report));

            Long memberTeamId = member.getTeam().getTeamId();
            if (Objects.equals(memberTeamId, homeId)) {
                homeGoals += goals;
            } else if (Objects.equals(memberTeamId, awayId)) {
                awayGoals += goals;
            }
        }

        fixture.setHomeScore(homeGoals);
        fixture.setAwayScore(awayGoals);
        if (!wasApproved) {
            fixture.setStatus(FixtureStatus.REPORTED);
        }
        fixtureRepository.save(fixture);

        var me = authService.me();
        int entryCount = saved.size();
        logEdit(fixture, action,
                me.getDisplayName() + " saved " + entryCount + " report entr"
                        + (entryCount == 1 ? "y" : "ies")
                        + " (" + homeGoals + "-" + awayGoals + ")");

        if (notifyAdmin && !admin) {
            notificationService.notify(UserRole.ADMIN, 1L,
                    "Match report submitted",
                    fixture.getHomeTeam().getName() + " vs " + fixture.getAwayTeam().getName()
                            + " (" + homeGoals + "-" + awayGoals + ") awaits approval",
                    NotificationType.REPORT_SUBMITTED, "FIXTURE", fixtureId);
        }

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<MatchReportResponse> approveReport(Long reportId) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException("Only admin can approve reports", HttpStatus.FORBIDDEN);
        }

        MatchReport report = matchReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("Report not found", HttpStatus.NOT_FOUND));

        Long fixtureId = report.getFixture().getId();
        Fixture fixture = fixtureService.getFixture(fixtureId);

        if (fixture.getStatus() == FixtureStatus.APPROVED) {
            throw new BusinessException("Reports already approved for this fixture");
        }

        List<MatchReport> reports = matchReportRepository.findByFixtureId(fixtureId);
        int homeGoals = 0;
        int awayGoals = 0;
        Long homeId = fixture.getHomeTeam().getTeamId();
        Long awayId = fixture.getAwayTeam().getTeamId();

        for (MatchReport r : reports) {
            if (r.getStatus() == ReportStatus.APPROVED) {
                throw new BusinessException("Report already approved and cannot be edited");
            }
            r.setStatus(ReportStatus.APPROVED);
            matchReportRepository.save(r);

            TeamMember member = r.getTeamMember();
            int goals = r.getGoal() != null ? r.getGoal() : 0;
            Long memberTeamId = member.getTeam().getTeamId();
            if (Objects.equals(memberTeamId, homeId)) {
                homeGoals += goals;
            } else if (Objects.equals(memberTeamId, awayId)) {
                awayGoals += goals;
            }
        }

        fixture.setHomeScore(homeGoals);
        fixture.setAwayScore(awayGoals);
        fixture.setStatus(FixtureStatus.APPROVED);
        fixtureRepository.save(fixture);
        suspensionService.processApprovedReports(fixtureId);

        var me = authService.me();
        logEdit(fixture, "APPROVED",
                me.getDisplayName() + " approved the match report (" + homeGoals + "-" + awayGoals + ")");

        notificationService.notify(UserRole.TEAM, homeId,
                "Match result approved",
                fixture.getHomeTeam().getName() + " " + homeGoals + "-" + awayGoals + " "
                        + fixture.getAwayTeam().getName(),
                NotificationType.REPORT_APPROVED, "FIXTURE", fixtureId);
        notificationService.notify(UserRole.TEAM, awayId,
                "Match result approved",
                fixture.getHomeTeam().getName() + " " + homeGoals + "-" + awayGoals + " "
                        + fixture.getAwayTeam().getName(),
                NotificationType.REPORT_APPROVED, "FIXTURE", fixtureId);

        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchReportCommentResponse> getComments(Long fixtureId) {
        fixtureService.getFixture(fixtureId);
        return commentRepository.findByFixtureIdOrderByCreatedAtAsc(fixtureId).stream()
                .map(this::toCommentResponse).collect(Collectors.toList());
    }

    @Transactional
    public MatchReportCommentResponse addComment(Long fixtureId, MatchReportCommentRequest request) {
        if (!securityUtils.isAdmin() && !securityUtils.isReferee()) {
            throw new BusinessException("Only admin or referee can comment", HttpStatus.FORBIDDEN);
        }
        Fixture fixture = fixtureService.getFixture(fixtureId);
        if (securityUtils.isReferee()) {
            Long refereeId = securityUtils.currentEntityId();
            if (fixture.getReferee() == null || !fixture.getReferee().getRefereeId().equals(refereeId)) {
                throw new BusinessException("You are not assigned to this fixture", HttpStatus.FORBIDDEN);
            }
        }
        var me = authService.me();
        MatchReportComment comment = MatchReportComment.builder()
                .fixture(fixture)
                .body(request.getBody().trim())
                .authorRole(me.getRole())
                .authorName(me.getDisplayName())
                .authorEntityId(me.getEntityId())
                .build();
        return toCommentResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<MatchReportEditLogResponse> getEditLogs(Long fixtureId) {
        fixtureService.getFixture(fixtureId);
        return editLogRepository.findByFixtureIdOrderByCreatedAtDesc(fixtureId).stream()
                .map(this::toEditLogResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchReportResponse> getPendingReports() {
        return matchReportRepository.findByStatus(ReportStatus.SUBMITTED).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchReportResponse> getApprovedReports() {
        return matchReportRepository.findByStatus(ReportStatus.APPROVED).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MatchReportResponse> getAllReports() {
        return matchReportRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    private void logEdit(Fixture fixture, String action, String summary) {
        var me = authService.me();
        editLogRepository.save(MatchReportEditLog.builder()
                .fixture(fixture)
                .editorRole(me.getRole())
                .editorName(me.getDisplayName())
                .editorEntityId(me.getEntityId())
                .action(action)
                .summary(summary)
                .build());
    }

    private MatchReportResponse toResponse(MatchReport report) {
        Fixture fixture = report.getFixture();
        var member = report.getTeamMember();
        var referee = report.getSubmittedByReferee();
        return MatchReportResponse.builder()
                .reportId(report.getReportId())
                .fixtureId(fixture.getId())
                .matchLabel(fixture.getHomeTeam().getName() + " vs " + fixture.getAwayTeam().getName())
                .homeScore(fixture.getHomeScore())
                .awayScore(fixture.getAwayScore())
                .teamMemberId(member.getMemberId())
                .playerName(member.getFname() + " " + member.getLname())
                .teamName(member.getTeam().getName())
                .goal(report.getGoal())
                .goalMin(report.getGoalMin())
                .card(report.getCard())
                .cardMin(report.getCardMin())
                .week(report.getWeek())
                .status(report.getStatus())
                .submittedByRefereeId(referee.getRefereeId())
                .refereeName(referee.getFname() + " " + referee.getLname())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }

    private MatchReportCommentResponse toCommentResponse(MatchReportComment c) {
        return MatchReportCommentResponse.builder()
                .id(c.getId())
                .fixtureId(c.getFixture().getId())
                .body(c.getBody())
                .authorRole(c.getAuthorRole())
                .authorName(c.getAuthorName())
                .authorEntityId(c.getAuthorEntityId())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private MatchReportEditLogResponse toEditLogResponse(MatchReportEditLog e) {
        return MatchReportEditLogResponse.builder()
                .id(e.getId())
                .fixtureId(e.getFixture().getId())
                .editorRole(e.getEditorRole())
                .editorName(e.getEditorName())
                .editorEntityId(e.getEditorEntityId())
                .action(e.getAction())
                .summary(e.getSummary())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
