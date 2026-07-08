package com.ferwafa.sanction;

import com.ferwafa.sanction.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Club Sanctions & Commissioner Reports")
public class SanctionController {

    private final SanctionService sanctionService;

    @PostMapping("/api/commissioner-reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submit commissioner report for a fixture")
    public ResponseEntity<CommissionerReportResponse> submit(@Valid @RequestBody CommissionerReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanctionService.submitCommissionerReport(request));
    }

    @GetMapping("/api/commissioner-reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List commissioner reports")
    public ResponseEntity<List<CommissionerReportResponse>> listReports() {
        return ResponseEntity.ok(sanctionService.listCommissionerReports());
    }

    @PostMapping("/api/club-sanctions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Issue a club-level sanction")
    public ResponseEntity<ClubSanctionResponse> issue(@Valid @RequestBody ClubSanctionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sanctionService.issueSanction(request));
    }

    @GetMapping("/api/club-sanctions")
    @Operation(summary = "List active club sanctions for a season")
    public ResponseEntity<List<ClubSanctionResponse>> listSanctions(
            @RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(sanctionService.listSanctions(season));
    }
}
