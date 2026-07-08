package com.ferwafa.transfer;

import com.ferwafa.transfer.dto.TransferWindowResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer-windows")
@RequiredArgsConstructor
@Tag(name = "Transfer Windows")
public class TransferWindowController {

    private final TransferWindowService transferWindowService;

    @GetMapping
    @Operation(summary = "List transfer windows for a season")
    public ResponseEntity<List<TransferWindowResponse>> list(
            @RequestParam(defaultValue = "2025/26") String season) {
        return ResponseEntity.ok(transferWindowService.list(season));
    }

    @GetMapping("/open")
    @Operation(summary = "List currently open transfer windows")
    public ResponseEntity<List<TransferWindowResponse>> open() {
        return ResponseEntity.ok(transferWindowService.listOpen());
    }
}
