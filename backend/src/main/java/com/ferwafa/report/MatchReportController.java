package com.ferwafa.report;

import com.ferwafa.report.dto.MatchReportResponse;
import com.ferwafa.report.dto.MatchReportSubmitRequest;
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
@Tag(name = "Match Reports")
public class MatchReportController {

    private final MatchReportService matchReportService;

    @GetMapping("/api/fixtures/{id}/report")
    @Operation(summary = "Get match report for fixture")
    public ResponseEntity<List<MatchReportResponse>> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(matchReportService.getReports(id));
    }

    @PostMapping("/api/fixtures/{id}/report")
    @PreAuthorize("hasRole('REFEREE')")
    @Operation(summary = "Submit match report (referee)")
    public ResponseEntity<List<MatchReportResponse>> submitReport(
            @PathVariable Long id, @Valid @RequestBody MatchReportSubmitRequest request) {
        return ResponseEntity.ok(matchReportService.submitReport(id, request));
    }

    @PutMapping("/api/reports/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve match report (admin)")
    public ResponseEntity<List<MatchReportResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(matchReportService.approveReport(id));
    }
}
