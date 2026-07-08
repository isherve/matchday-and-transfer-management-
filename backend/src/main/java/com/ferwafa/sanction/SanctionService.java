package com.ferwafa.sanction;

import com.ferwafa.auth.UserRepository;
import com.ferwafa.common.BusinessException;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.fixture.FixtureService;
import com.ferwafa.notification.NotificationService;
import com.ferwafa.sanction.dto.*;
import com.ferwafa.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanctionService {

    private final CommissionerReportRepository commissionerReportRepository;
    private final ClubSanctionRepository clubSanctionRepository;
    private final FixtureService fixtureService;
    private final TeamService teamService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    @Transactional
    public CommissionerReportResponse submitCommissionerReport(CommissionerReportRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException("Only admin can submit commissioner reports", HttpStatus.FORBIDDEN);
        }
        var fixture = fixtureService.getFixture(request.getFixtureId());
        if (commissionerReportRepository.findByFixtureId(request.getFixtureId()).isPresent()) {
            throw new BusinessException("Commissioner report already exists for this fixture");
        }
        var admin = userRepository.findById(securityUtils.currentEntityId()).orElse(null);
        CommissionerReport report = CommissionerReport.builder()
                .fixture(fixture)
                .submittedByAdmin(admin)
                .pitchCondition(request.getPitchCondition())
                .crowdBehavior(request.getCrowdBehavior())
                .securityIncidents(request.getSecurityIncidents())
                .technicalIssues(request.getTechnicalIssues())
                .otherNotes(request.getOtherNotes())
                .status("SUBMITTED")
                .build();
        return toCommResponse(commissionerReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<CommissionerReportResponse> listCommissionerReports() {
        return commissionerReportRepository.findAll().stream()
                .map(this::toCommResponse).collect(Collectors.toList());
    }

    @Transactional
    public ClubSanctionResponse issueSanction(ClubSanctionRequest request) {
        if (!securityUtils.isAdmin()) {
            throw new BusinessException("Only admin can issue club sanctions", HttpStatus.FORBIDDEN);
        }
        var team = teamService.getTeam(request.getTeamId());
        var admin = userRepository.findById(securityUtils.currentEntityId()).orElse(null);
        CommissionerReport linked = null;
        if (request.getCommissionerReportId() != null) {
            linked = commissionerReportRepository.findById(request.getCommissionerReportId())
                    .orElseThrow(() -> new BusinessException("Commissioner report not found", HttpStatus.NOT_FOUND));
        }

        int points = request.getPointsDeducted() != null ? request.getPointsDeducted() : 0;
        ClubSanction sanction = ClubSanction.builder()
                .team(team)
                .season(request.getSeason())
                .sanctionType(request.getSanctionType())
                .pointsDeducted(points)
                .fineAmount(request.getFineAmount())
                .stadiumBanMatches(request.getStadiumBanMatches())
                .reason(request.getReason())
                .commissionerReport(linked)
                .issuedByAdmin(admin)
                .issuedDate(LocalDate.now())
                .active(true)
                .build();
        ClubSanction saved = clubSanctionRepository.save(sanction);

        notificationService.notify(UserRole.TEAM, team.getTeamId(),
                "Club sanction issued",
                request.getSanctionType() + ": " + request.getReason(),
                NotificationType.CLUB_SANCTION, "CLUB_SANCTION", saved.getId());

        return toSanctionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ClubSanctionResponse> listSanctions(String season) {
        return clubSanctionRepository.findBySeasonAndActiveTrue(season).stream()
                .map(this::toSanctionResponse).collect(Collectors.toList());
    }

    private CommissionerReportResponse toCommResponse(CommissionerReport r) {
        return CommissionerReportResponse.builder()
                .id(r.getId())
                .fixtureId(r.getFixture().getId())
                .matchLabel(r.getFixture().getHomeTeam().getName() + " vs " + r.getFixture().getAwayTeam().getName())
                .pitchCondition(r.getPitchCondition())
                .crowdBehavior(r.getCrowdBehavior())
                .securityIncidents(r.getSecurityIncidents())
                .technicalIssues(r.getTechnicalIssues())
                .otherNotes(r.getOtherNotes())
                .status(r.getStatus())
                .build();
    }

    private ClubSanctionResponse toSanctionResponse(ClubSanction s) {
        return ClubSanctionResponse.builder()
                .id(s.getId())
                .teamId(s.getTeam().getTeamId())
                .teamName(s.getTeam().getName())
                .season(s.getSeason())
                .sanctionType(s.getSanctionType())
                .pointsDeducted(s.getPointsDeducted())
                .fineAmount(s.getFineAmount())
                .stadiumBanMatches(s.getStadiumBanMatches())
                .reason(s.getReason())
                .commissionerReportId(s.getCommissionerReport() != null ? s.getCommissionerReport().getId() : null)
                .issuedDate(s.getIssuedDate())
                .active(Boolean.TRUE.equals(s.getActive()))
                .build();
    }
}
