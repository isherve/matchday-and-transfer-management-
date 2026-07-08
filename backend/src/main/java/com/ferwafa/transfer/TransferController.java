package com.ferwafa.transfer;

import com.ferwafa.transfer.dto.TransferRequest;
import com.ferwafa.transfer.dto.TransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfers")
public class TransferController {

    private final TransferService transferService;

    @GetMapping
    @Operation(summary = "List transfers")
    public ResponseEntity<List<TransferResponse>> list() {
        return ResponseEntity.ok(transferService.findAll());
    }

    @PostMapping
    @Operation(summary = "Create transfer request")
    public ResponseEntity<TransferResponse> create(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(request));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve transfer")
    public ResponseEntity<TransferResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.approve(id));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject transfer")
    public ResponseEntity<TransferResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.reject(id));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete transfer")
    public ResponseEntity<TransferResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.complete(id));
    }
}
