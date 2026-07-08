package com.ferwafa.fixture;

import com.ferwafa.fixture.dto.LineupRequest;
import com.ferwafa.fixture.dto.LineupResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lineups")
public class LineupController {

    private final LineupService lineupService;

    @GetMapping("/api/fixtures/{id}/lineup")
    @Operation(summary = "Get lineup for fixture")
    public ResponseEntity<List<LineupResponse>> getLineup(
            @PathVariable Long id,
            @RequestParam Long teamId) {
        return ResponseEntity.ok(lineupService.getLineup(id, teamId));
    }

    @PostMapping("/api/fixtures/{id}/lineup")
    @Operation(summary = "Save lineup (validates suspensions)")
    public ResponseEntity<List<LineupResponse>> saveLineup(
            @PathVariable Long id, @Valid @RequestBody LineupRequest request) {
        return ResponseEntity.ok(lineupService.saveLineup(id, request));
    }
}
