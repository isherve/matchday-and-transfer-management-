package com.ferwafa.standings;

import com.ferwafa.common.FixtureStatus;
import com.ferwafa.fixture.Fixture;
import com.ferwafa.fixture.FixtureRepository;
import com.ferwafa.member.TeamMember;
import com.ferwafa.report.MatchReport;
import com.ferwafa.report.MatchReportRepository;
import com.ferwafa.sanction.ClubSanctionRepository;
import com.ferwafa.standings.dto.LeaderboardEntry;
import com.ferwafa.standings.dto.StandingRow;
import com.ferwafa.team.Team;
import com.ferwafa.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StandingsService {

    private final FixtureRepository fixtureRepository;
    private final TeamRepository teamRepository;
    private final ClubSanctionRepository clubSanctionRepository;
    private final MatchReportRepository matchReportRepository;

    @Transactional(readOnly = true)
    public List<StandingRow> getStandings(String season) {
        List<Team> teams = teamRepository.findAll();
        Map<Long, TeamStats> stats = new LinkedHashMap<>();
        for (Team t : teams) {
            stats.put(t.getTeamId(), new TeamStats(t));
        }

        List<Fixture> approved = fixtureRepository.findBySeasonOrderByWeekAscMatchDateAsc(season).stream()
                .filter(f -> f.getStatus() == FixtureStatus.APPROVED
                        && f.getHomeScore() != null && f.getAwayScore() != null)
                .collect(Collectors.toList());

        // Track head-to-head: points earned by A vs B
        Map<String, Integer> h2hPoints = new HashMap<>();

        for (Fixture f : approved) {
            Long homeId = f.getHomeTeam().getTeamId();
            Long awayId = f.getAwayTeam().getTeamId();
            TeamStats home = stats.get(homeId);
            TeamStats away = stats.get(awayId);
            if (home == null || away == null) continue;

            int hs = f.getHomeScore();
            int as = f.getAwayScore();
            home.played++;
            away.played++;
            home.gf += hs;
            home.ga += as;
            away.gf += as;
            away.ga += hs;

            if (hs > as) {
                home.won++;
                away.lost++;
                home.points += 3;
                putH2h(h2hPoints, homeId, awayId, 3);
                putH2h(h2hPoints, awayId, homeId, 0);
            } else if (hs < as) {
                away.won++;
                home.lost++;
                away.points += 3;
                putH2h(h2hPoints, awayId, homeId, 3);
                putH2h(h2hPoints, homeId, awayId, 0);
            } else {
                home.drawn++;
                away.drawn++;
                home.points += 1;
                away.points += 1;
                putH2h(h2hPoints, homeId, awayId, 1);
                putH2h(h2hPoints, awayId, homeId, 1);
            }
        }

        for (TeamStats s : stats.values()) {
            s.pointsDeducted = clubSanctionRepository.sumActivePointsDeduction(s.team.getTeamId(), season);
            s.points = Math.max(0, s.points - s.pointsDeducted);
        }

        List<TeamStats> sorted = new ArrayList<>(stats.values());
        sorted.sort((a, b) -> {
            if (b.points != a.points) return Integer.compare(b.points, a.points);
            int gdA = a.gf - a.ga;
            int gdB = b.gf - b.ga;
            if (gdB != gdA) return Integer.compare(gdB, gdA);
            if (b.gf != a.gf) return Integer.compare(b.gf, a.gf);
            // Head-to-head tiebreaker
            int h2hA = h2hPoints.getOrDefault(a.team.getTeamId() + "-" + b.team.getTeamId(), 0);
            int h2hB = h2hPoints.getOrDefault(b.team.getTeamId() + "-" + a.team.getTeamId(), 0);
            if (h2hB != h2hA) return Integer.compare(h2hB, h2hA);
            return a.team.getName().compareToIgnoreCase(b.team.getName());
        });

        List<StandingRow> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            TeamStats s = sorted.get(i);
            rows.add(StandingRow.builder()
                    .position(i + 1)
                    .teamId(s.team.getTeamId())
                    .teamName(s.team.getName())
                    .played(s.played)
                    .won(s.won)
                    .drawn(s.drawn)
                    .lost(s.lost)
                    .goalsFor(s.gf)
                    .goalsAgainst(s.ga)
                    .goalDifference(s.gf - s.ga)
                    .points(s.points)
                    .pointsDeducted(s.pointsDeducted)
                    .build());
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getTopScorers(String season, int limit) {
        Map<Long, Agg> agg = aggregateReports(season);
        return agg.values().stream()
                .filter(a -> a.goals > 0)
                .sorted(Comparator.comparingInt((Agg a) -> a.goals).reversed()
                        .thenComparing(a -> a.playerName))
                .limit(limit)
                .map(a -> toLeaderboard(a))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getCardsLeaderboard(String season, int limit) {
        Map<Long, Agg> agg = aggregateReports(season);
        List<LeaderboardEntry> ranked = agg.values().stream()
                .filter(a -> a.yellows > 0 || a.reds > 0)
                .sorted(Comparator
                        .comparingInt((Agg a) -> a.reds).reversed()
                        .thenComparingInt(a -> a.yellows).reversed()
                        .thenComparing(a -> a.playerName))
                .limit(limit)
                .map(this::toLeaderboard)
                .collect(Collectors.toList());
        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return ranked;
    }

    private Map<Long, Agg> aggregateReports(String season) {
        Set<Long> approvedFixtureIds = fixtureRepository.findBySeasonOrderByWeekAscMatchDateAsc(season).stream()
                .filter(f -> f.getStatus() == FixtureStatus.APPROVED)
                .map(Fixture::getId)
                .collect(Collectors.toSet());

        Map<Long, Agg> agg = new HashMap<>();
        for (MatchReport r : matchReportRepository.findAll()) {
            if (!approvedFixtureIds.contains(r.getFixture().getId())) continue;
            if (r.getStatus() != com.ferwafa.common.ReportStatus.APPROVED) continue;
            TeamMember m = r.getTeamMember();
            Agg a = agg.computeIfAbsent(m.getMemberId(), id -> {
                Agg x = new Agg();
                x.memberId = m.getMemberId();
                x.playerName = m.getFname() + " " + m.getLname();
                x.teamId = m.getTeam().getTeamId();
                x.teamName = m.getTeam().getName();
                return x;
            });
            a.goals += r.getGoal() != null ? r.getGoal() : 0;
            if (r.getCard() == com.ferwafa.common.CardType.YELLOW) a.yellows++;
            if (r.getCard() == com.ferwafa.common.CardType.RED) a.reds++;
        }
        return agg;
    }

    private LeaderboardEntry toLeaderboard(Agg a) {
        return LeaderboardEntry.builder()
                .rank(0)
                .memberId(a.memberId)
                .playerName(a.playerName)
                .teamId(a.teamId)
                .teamName(a.teamName)
                .goals(a.goals)
                .yellowCards(a.yellows)
                .redCards(a.reds)
                .build();
    }

    private void putH2h(Map<String, Integer> map, Long a, Long b, int pts) {
        String key = a + "-" + b;
        map.merge(key, pts, Integer::sum);
    }

    private static class TeamStats {
        Team team;
        int played, won, drawn, lost, gf, ga, points, pointsDeducted;
        TeamStats(Team team) { this.team = team; }
    }

    private static class Agg {
        Long memberId;
        String playerName;
        Long teamId;
        String teamName;
        int goals, yellows, reds;
    }
}
