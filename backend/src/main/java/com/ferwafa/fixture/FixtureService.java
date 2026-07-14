package com.ferwafa.fixture;

import com.ferwafa.common.BusinessException;
import com.ferwafa.common.FixtureStatus;
import com.ferwafa.common.NotificationType;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.fixture.dto.AssignRefereeRequest;
import com.ferwafa.fixture.dto.FixtureRequest;
import com.ferwafa.fixture.dto.FixtureResponse;
import com.ferwafa.fixture.dto.PostponeFixtureRequest;
import com.ferwafa.notification.NotificationService;
import com.ferwafa.referee.Referee;
import com.ferwafa.referee.RefereeService;
import com.ferwafa.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;
    private final TeamService teamService;
    private final RefereeService refereeService;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<FixtureResponse> findAll() {
        if (securityUtils.isReferee()) {
            return fixtureRepository.findByRefereeRefereeIdOrderByMatchDateAsc(securityUtils.currentEntityId())
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        if (securityUtils.isTeam()) {
            Long teamId = securityUtils.currentEntityId();
            return fixtureRepository.findByHomeTeamTeamIdOrAwayTeamTeamIdOrderByMatchDateAsc(teamId, teamId)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return fixtureRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FixtureResponse> findPublic() {
        return fixtureRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FixtureResponse findById(Long id) {
        return toResponse(getFixture(id));
    }

    @Transactional
    public FixtureResponse create(FixtureRequest request) {
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new BusinessException("Home and away teams must be different");
        }
        Fixture fixture = Fixture.builder()
                .homeTeam(teamService.getTeam(request.getHomeTeamId()))
                .awayTeam(teamService.getTeam(request.getAwayTeamId()))
                .week(request.getWeek())
                .stadium(request.getStadium())
                .matchDate(request.getMatchDate())
                .matchTime(request.getMatchTime())
                .season(request.getSeason())
                .status(FixtureStatus.SCHEDULED)
                .build();
        return toResponse(fixtureRepository.save(fixture));
    }

    @Transactional
    public FixtureResponse update(Long id, FixtureRequest request) {
        Fixture fixture = getFixture(id);
        fixture.setHomeTeam(teamService.getTeam(request.getHomeTeamId()));
        fixture.setAwayTeam(teamService.getTeam(request.getAwayTeamId()));
        fixture.setWeek(request.getWeek());
        fixture.setStadium(request.getStadium());
        fixture.setMatchDate(request.getMatchDate());
        fixture.setMatchTime(request.getMatchTime());
        fixture.setSeason(request.getSeason());
        return toResponse(fixtureRepository.save(fixture));
    }

    @Transactional
    public FixtureResponse assignReferee(Long id, AssignRefereeRequest request) {
        Fixture fixture = getFixture(id);
        Referee referee = refereeService.getReferee(request.getRefereeId());

        List<Fixture> sameDay = fixtureRepository.findByRefereeRefereeIdOrderByMatchDateAsc(referee.getRefereeId());
        boolean conflict = sameDay.stream().anyMatch(f ->
                !f.getId().equals(fixture.getId())
                        && f.getMatchDate().equals(fixture.getMatchDate())
                        && f.getStatus() != FixtureStatus.POSTPONED);
        if (conflict) {
            throw new BusinessException(
                    "Referee already assigned to another fixture on " + fixture.getMatchDate());
        }

        fixture.setReferee(referee);
        fixture.setStatus(FixtureStatus.REFEREE_ASSIGNED);
        Fixture saved = fixtureRepository.save(fixture);

        notificationService.notify(UserRole.REFEREE, referee.getRefereeId(),
                "Match assignment",
                "You have been assigned: " + saved.getHomeTeam().getName() + " vs "
                        + saved.getAwayTeam().getName() + " on " + saved.getMatchDate(),
                NotificationType.REFEREE_ASSIGNED, "FIXTURE", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public FixtureResponse postpone(Long id, PostponeFixtureRequest request) {
        Fixture fixture = getFixture(id);
        if (fixture.getStatus() == FixtureStatus.APPROVED) {
            throw new BusinessException("Cannot postpone an approved fixture");
        }
        if (fixture.getOriginalMatchDate() == null) {
            fixture.setOriginalMatchDate(fixture.getMatchDate());
        }
        fixture.setPostponementReason(request.getReason());
        fixture.setMatchDate(request.getNewMatchDate());
        if (request.getNewMatchTime() != null) {
            fixture.setMatchTime(request.getNewMatchTime());
        }
        fixture.setStatus(FixtureStatus.POSTPONED);
        Fixture saved = fixtureRepository.save(fixture);

        String msg = saved.getHomeTeam().getName() + " vs " + saved.getAwayTeam().getName()
                + " postponed to " + saved.getMatchDate() + ". Reason: " + request.getReason();
        notificationService.notify(UserRole.TEAM, saved.getHomeTeam().getTeamId(),
                "Fixture postponed", msg, NotificationType.FIXTURE_POSTPONED, "FIXTURE", saved.getId());
        notificationService.notify(UserRole.TEAM, saved.getAwayTeam().getTeamId(),
                "Fixture postponed", msg, NotificationType.FIXTURE_POSTPONED, "FIXTURE", saved.getId());
        if (saved.getReferee() != null) {
            notificationService.notify(UserRole.REFEREE, saved.getReferee().getRefereeId(),
                    "Fixture postponed", msg, NotificationType.FIXTURE_POSTPONED, "FIXTURE", saved.getId());
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FixtureResponse> findByTeamAndSeason(Long teamId, String season) {
        return fixtureRepository.findByTeamAndSeason(teamId, season).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Fixture getFixture(Long id) {
        return fixtureRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Fixture not found", HttpStatus.NOT_FOUND));
    }

    public FixtureResponse toResponse(Fixture fixture) {
        return FixtureResponse.builder()
                .id(fixture.getId())
                .homeTeamId(fixture.getHomeTeam().getTeamId())
                .homeTeamName(fixture.getHomeTeam().getName())
                .homeTeamLogo(fixture.getHomeTeam().getLogo())
                .awayTeamId(fixture.getAwayTeam().getTeamId())
                .awayTeamName(fixture.getAwayTeam().getName())
                .awayTeamLogo(fixture.getAwayTeam().getLogo())
                .week(fixture.getWeek())
                .stadium(fixture.getStadium())
                .matchDate(fixture.getMatchDate())
                .matchTime(fixture.getMatchTime())
                .season(fixture.getSeason())
                .status(fixture.getStatus())
                .refereeId(fixture.getReferee() != null ? fixture.getReferee().getRefereeId() : null)
                .refereeName(fixture.getReferee() != null ?
                        fixture.getReferee().getFname() + " " + fixture.getReferee().getLname() : null)
                .homeScore(fixture.getHomeScore())
                .awayScore(fixture.getAwayScore())
                .postponementReason(fixture.getPostponementReason())
                .originalMatchDate(fixture.getOriginalMatchDate())
                .build();
    }
}
