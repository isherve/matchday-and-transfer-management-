package com.ferwafa.web;

import com.ferwafa.fixture.FixtureService;
import com.ferwafa.standings.StandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/public")
@RequiredArgsConstructor
public class PublicWebController {

    private final StandingsService standingsService;
    private final FixtureService fixtureService;

    @GetMapping({"", "/", "/standings"})
    public String standings(@RequestParam(defaultValue = "2025/26") String season, Model model) {
        model.addAttribute("season", season);
        model.addAttribute("standings", standingsService.getStandings(season));
        model.addAttribute("fixtures", fixtureService.findPublic());
        model.addAttribute("topScorers", standingsService.getTopScorers(season, 10));
        model.addAttribute("cards", standingsService.getCardsLeaderboard(season, 10));
        return "public/standings";
    }

    @GetMapping("/api/standings")
    @ResponseBody
    public ResponseEntity<?> publicStandingsApi(@RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(standingsService.getStandings(season));
    }

    @GetMapping("/api/fixtures")
    @ResponseBody
    public ResponseEntity<?> publicFixturesApi() {
        return ResponseEntity.ok(fixtureService.findPublic());
    }

    @GetMapping("/api/top-scorers")
    @ResponseBody
    public ResponseEntity<?> publicTopScorers(@RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(standingsService.getTopScorers(season, 20));
    }
}
