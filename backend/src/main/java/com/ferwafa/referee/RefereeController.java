package com.ferwafa.referee;

import com.ferwafa.referee.dto.RefereeRequest;
import com.ferwafa.referee.dto.RefereeResponse;
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
@RequestMapping("/api/referees")
@RequiredArgsConstructor
@Tag(name = "Referees")
public class RefereeController {

    private final RefereeService refereeService;

    @GetMapping
    @Operation(summary = "List all referees")
    public ResponseEntity<List<RefereeResponse>> list() {
        return ResponseEntity.ok(refereeService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a referee")
    public ResponseEntity<RefereeResponse> create(@Valid @RequestBody RefereeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(refereeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a referee")
    public ResponseEntity<RefereeResponse> update(@PathVariable Long id, @Valid @RequestBody RefereeRequest request) {
        return ResponseEntity.ok(refereeService.update(id, request));
    }
}
