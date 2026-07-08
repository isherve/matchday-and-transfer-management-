package com.ferwafa.discipline;

import com.ferwafa.discipline.dto.SuspensionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/suspensions")
@RequiredArgsConstructor
@Tag(name = "Discipline")
public class DisciplineController {

    private final SuspensionService suspensionService;

    @GetMapping
    @Operation(summary = "Get suspended/ineligible players for a fixture")
    public ResponseEntity<List<SuspensionDto>> getSuspensions(
            @PathVariable Long teamId,
            @RequestParam Long fixtureId) {
        return ResponseEntity.ok(suspensionService.getSuspendedPlayers(teamId, fixtureId));
    }
}
