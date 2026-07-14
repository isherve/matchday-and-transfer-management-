package com.ferwafa.discipline;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.CardType;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.SuspensionReason;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.DisciplineProperties;
import com.ferwafa.discipline.dto.SuspensionDto;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.member.TeamMember;
import com.ferwafa.member.TeamMemberRepository;
import com.ferwafa.notification.NotificationService;
import com.ferwafa.report.MatchReport;
import com.ferwafa.report.MatchReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuspensionService {

    private final DisciplinaryRecordRepository disciplinaryRecordRepository;
    private final FixtureRepository fixtureRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MatchReportRepository matchReportRepository;
    private final DisciplineProperties disciplineProperties;
    private final NotificationService notificationService;

    /**
     * Process approved match reports and create/update disciplinary records with suspensions.
     */
    @Transactional
    public void processApprovedReports(Long fixtureId) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new BusinessException("Fixture not found", HttpStatus.NOT_FOUND));

        List<MatchReport> approvedReports = matchReportRepository.findByFixtureIdAndStatus(
                fixtureId, com.ferwafa.common.ReportStatus.APPROVED);

        Fixture nextFixtureForTeam = null;

        for (MatchReport report : approvedReports) {
            if (report.getCard() == CardType.NONE) continue;

            TeamMember member = report.getTeamMember();
            Long teamId = member.getTeam().getTeamId();

            DisciplinaryRecord record = DisciplinaryRecord.builder()
                    .teamMember(member)
                    .fixture(fixture)
                    .week(fixture.getWeek())
                    .cardType(report.getCard())
                    .cardMin(report.getCardMin())
                    .suspensionServed(false)
                    .build();

            if (report.getCard() == CardType.RED) {
                record.setSuspensionReason(SuspensionReason.RED_CARD);
                Fixture nextFixture = findNextFixtureForTeam(teamId, fixture.getWeek(), fixture.getSeason());
                if (nextFixture != null) {
                    record.setSuspensionFixture(nextFixture);
                }
                disciplinaryRecordRepository.save(record);
                notificationService.notify(UserRole.TEAM, teamId,
                        "Player suspended (red card)",
                        member.getFname() + " " + member.getLname() + " is suspended for the next match",
                        NotificationType.PLAYER_SUSPENDED, "MEMBER", member.getMemberId());
            } else if (report.getCard() == CardType.YELLOW) {
                int yellowCount = countUnservedYellows(member.getMemberId(), fixture.getWeek(), fixture.getSeason());
                if (yellowCount + 1 >= disciplineProperties.getYellowCardThreshold()) {
                    record.setSuspensionReason(SuspensionReason.TWO_YELLOWS);
                    Fixture nextFixture = findNextFixtureForTeam(teamId, fixture.getWeek(), fixture.getSeason());
                    if (nextFixture != null) {
                        record.setSuspensionFixture(nextFixture);
                    }
                    markYellowsAsServedForSuspension(member.getMemberId(), fixture.getWeek());
                    disciplinaryRecordRepository.save(record);
                    notificationService.notify(UserRole.TEAM, teamId,
                            "Player suspended (accumulated yellows)",
                            member.getFname() + " " + member.getLname() + " is suspended for the next match",
                            NotificationType.PLAYER_SUSPENDED, "MEMBER", member.getMemberId());
                } else {
                    disciplinaryRecordRepository.save(record);
                }
            } else {
                disciplinaryRecordRepository.save(record);
            }
        }
    }

    private int countUnservedYellows(Long memberId, int currentWeek, String season) {
        List<DisciplinaryRecord> records = disciplinaryRecordRepository
                .findByTeamMemberMemberIdOrderByWeekAsc(memberId);
        return (int) records.stream()
                .filter(r -> r.getCardType() == CardType.YELLOW)
                .filter(r -> r.getSuspensionReason() == null)
                .filter(r -> isSameSeason(r.getFixture(), season))
                .count();
    }

    private void markYellowsAsServedForSuspension(Long memberId, int week) {
        List<DisciplinaryRecord> yellows = disciplinaryRecordRepository
                .findByTeamMemberMemberIdOrderByWeekAsc(memberId).stream()
                .filter(r -> r.getCardType() == CardType.YELLOW)
                .filter(r -> r.getSuspensionReason() == null)
                .limit(disciplineProperties.getYellowCardThreshold())
                .collect(Collectors.toList());
        for (DisciplinaryRecord yellow : yellows) {
            yellow.setSuspensionServed(true);
            disciplinaryRecordRepository.save(yellow);
        }
    }

    private boolean isSameSeason(Fixture fixture, String season) {
        return fixture.getSeason().equals(season);
    }

    private Fixture findNextFixtureForTeam(Long teamId, int afterWeek, String season) {
        List<Fixture> next = fixtureRepository.findNextFixturesForTeam(teamId, afterWeek, season);
        return next.isEmpty() ? null : next.get(0);
    }

    /**
     * Returns suspended/ineligible players for a team + upcoming fixture.
     */
    @Transactional(readOnly = true)
    public List<SuspensionDto> getSuspendedPlayers(Long teamId, Long fixtureId) {
        Fixture targetFixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new BusinessException("Fixture not found", HttpStatus.NOT_FOUND));

        List<TeamMember> players = teamMemberRepository.findByTeamTeamIdAndRoleInTeam(
                teamId, com.ferwafa.common.MemberRole.PLAYER);

        List<SuspensionDto> suspensions = new ArrayList<>();

        for (TeamMember player : players) {
            SuspensionDto suspension = checkPlayerSuspension(player, targetFixture);
            if (suspension != null) {
                suspensions.add(suspension);
            }
        }

        return suspensions;
    }

    public SuspensionDto checkPlayerSuspension(TeamMember player, Fixture targetFixture) {
        List<DisciplinaryRecord> activeSuspensions = disciplinaryRecordRepository
                .findActiveSuspensions(player.getMemberId());

        for (DisciplinaryRecord record : activeSuspensions) {
            if (record.getSuspensionFixture() == null) continue;

            if (record.getSuspensionFixture().getId().equals(targetFixture.getId())) {
                Fixture trigger = record.getFixture();
                return SuspensionDto.builder()
                        .memberId(player.getMemberId())
                        .playerName(player.getFname() + " " + player.getLname())
                        .playerNumber(player.getNumber())
                        .reason(record.getSuspensionReason())
                        .reasonLabel(formatReason(record.getSuspensionReason()))
                        .triggeringFixtureId(trigger.getId())
                        .triggeringMatch(trigger.getHomeTeam().getName() + " vs " + trigger.getAwayTeam().getName())
                        .triggeringWeek(trigger.getWeek())
                        .build();
            }
        }
        return null;
    }

    public boolean isPlayerSuspended(Long memberId, Long fixtureId) {
        TeamMember player = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Player not found", HttpStatus.NOT_FOUND));
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new BusinessException("Fixture not found", HttpStatus.NOT_FOUND));
        return checkPlayerSuspension(player, fixture) != null;
    }

    /**
     * Mark suspensions as served when a player is NOT in the lineup for their suspension fixture,
     * or after the suspension fixture is played/approved.
     */
    @Transactional
    public void markSuspensionsServed(Long fixtureId, Long teamId) {
        Fixture fixture = fixtureRepository.findById(fixtureId).orElse(null);
        if (fixture == null) return;

        List<DisciplinaryRecord> records = disciplinaryRecordRepository.findByTeamId(teamId);
        for (DisciplinaryRecord record : records) {
            if (Boolean.TRUE.equals(record.getSuspensionServed())) continue;
            if (record.getSuspensionFixture() == null) continue;
            if (!record.getSuspensionFixture().getId().equals(fixtureId)) continue;

            record.setSuspensionServed(true);
            record.setServedFixture(fixture);
            disciplinaryRecordRepository.save(record);
        }
    }

    @Transactional(readOnly = true)
    public List<DisciplinaryRecord> getAllPunishments() {
        return disciplinaryRecordRepository.findAll().stream()
                .filter(r -> r.getSuspensionReason() != null || r.getCardType() != CardType.NONE)
                .sorted(Comparator.comparing(DisciplinaryRecord::getWeek).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DisciplinaryRecord> getActiveSuspensions() {
        return disciplinaryRecordRepository.findAll().stream()
                .filter(r -> r.getSuspensionReason() != null && !Boolean.TRUE.equals(r.getSuspensionServed()))
                .sorted(Comparator.comparing(DisciplinaryRecord::getWeek).reversed())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Set<Long> getActiveSuspendedMemberIds(Long teamId) {
        return disciplinaryRecordRepository.findByTeamId(teamId).stream()
                .filter(r -> r.getSuspensionReason() != null && !Boolean.TRUE.equals(r.getSuspensionServed()))
                .map(r -> r.getTeamMember().getMemberId())
                .collect(Collectors.toSet());
    }

    private String formatReason(SuspensionReason reason) {
        return switch (reason) {
            case RED_CARD -> "Direct red card - suspended for next match";
            case TWO_YELLOWS -> "Accumulated yellow cards - suspended for next match";
        };
    }
}
