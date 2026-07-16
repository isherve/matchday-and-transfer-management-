package com.ferwafa.referee;

import com.ferwafa.referee.dto.DiaryRequest;
import com.ferwafa.referee.dto.DiaryResponse;
import com.ferwafa.referee.dto.MatchPrepRequest;
import com.ferwafa.referee.dto.MatchPrepResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('REFEREE')")
@Tag(name = "Referee Matchday Ops")
public class RefereeOpsController {

    private final RefereeOpsService refereeOpsService;

    @GetMapping("/api/fixtures/{id}/prep")
    @Operation(summary = "Get matchday prep checklist")
    public ResponseEntity<MatchPrepResponse> getPrep(@PathVariable Long id) {
        return ResponseEntity.ok(refereeOpsService.getPrep(id));
    }

    @PutMapping("/api/fixtures/{id}/prep")
    @Operation(summary = "Save matchday prep checklist")
    public ResponseEntity<MatchPrepResponse> savePrep(
            @PathVariable Long id, @Valid @RequestBody MatchPrepRequest request) {
        return ResponseEntity.ok(refereeOpsService.savePrep(id, request));
    }

    @GetMapping("/api/referee/diary")
    @Operation(summary = "List referee duty diary entries")
    public ResponseEntity<List<DiaryResponse>> listDiary() {
        return ResponseEntity.ok(refereeOpsService.listDiary());
    }

    @PostMapping("/api/referee/diary")
    @Operation(summary = "Add a duty diary entry")
    public ResponseEntity<DiaryResponse> addDiary(@Valid @RequestBody DiaryRequest request) {
        return ResponseEntity.ok(refereeOpsService.addDiary(request));
    }
}
