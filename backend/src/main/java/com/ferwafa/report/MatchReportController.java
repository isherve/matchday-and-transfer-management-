package com.ferwafa.report;

import com.ferwafa.report.dto.MatchReportCommentRequest;
import com.ferwafa.report.dto.MatchReportCommentResponse;
import com.ferwafa.report.dto.MatchReportEditLogResponse;
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

    @PutMapping("/api/fixtures/{id}/report")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Edit match report (admin)")
    public ResponseEntity<List<MatchReportResponse>> adminUpdateReport(
            @PathVariable Long id, @Valid @RequestBody MatchReportSubmitRequest request) {
        return ResponseEntity.ok(matchReportService.adminUpdateReport(id, request));
    }

    @GetMapping("/api/fixtures/{id}/report/comments")
    @Operation(summary = "List match report comments")
    public ResponseEntity<List<MatchReportCommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(matchReportService.getComments(id));
    }

    @PostMapping("/api/fixtures/{id}/report/comments")
    @PreAuthorize("hasAnyRole('ADMIN','REFEREE')")
    @Operation(summary = "Add a match report comment")
    public ResponseEntity<MatchReportCommentResponse> addComment(
            @PathVariable Long id, @Valid @RequestBody MatchReportCommentRequest request) {
        return ResponseEntity.ok(matchReportService.addComment(id, request));
    }

    @GetMapping("/api/fixtures/{id}/report/edits")
    @Operation(summary = "List match report edit history")
    public ResponseEntity<List<MatchReportEditLogResponse>> getEditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(matchReportService.getEditLogs(id));
    }

    @PutMapping("/api/reports/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve match report (admin)")
    public ResponseEntity<List<MatchReportResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(matchReportService.approveReport(id));
    }
}
