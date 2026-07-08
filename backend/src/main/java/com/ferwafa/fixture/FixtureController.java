package com.ferwafa.fixture;

import com.ferwafa.fixture.dto.AssignRefereeRequest;
import com.ferwafa.fixture.dto.FixtureRequest;
import com.ferwafa.fixture.dto.FixtureResponse;
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
@RequestMapping("/api/fixtures")
@RequiredArgsConstructor
@Tag(name = "Fixtures")
public class FixtureController {

    private final FixtureService fixtureService;

    @GetMapping
    @Operation(summary = "List fixtures")
    public ResponseEntity<List<FixtureResponse>> list() {
        return ResponseEntity.ok(fixtureService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fixture by id")
    public ResponseEntity<FixtureResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(fixtureService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a fixture")
    public ResponseEntity<FixtureResponse> create(@Valid @RequestBody FixtureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fixtureService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a fixture")
    public ResponseEntity<FixtureResponse> update(@PathVariable Long id, @Valid @RequestBody FixtureRequest request) {
        return ResponseEntity.ok(fixtureService.update(id, request));
    }

    @PutMapping("/{id}/assign-referee")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign referee to fixture")
    public ResponseEntity<FixtureResponse> assignReferee(@PathVariable Long id, @Valid @RequestBody AssignRefereeRequest request) {
        return ResponseEntity.ok(fixtureService.assignReferee(id, request));
    }

    @PutMapping("/{id}/postpone")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Postpone a fixture with new date and reason")
    public ResponseEntity<FixtureResponse> postpone(@PathVariable Long id,
                                                    @Valid @RequestBody com.ferwafa.fixture.dto.PostponeFixtureRequest request) {
        return ResponseEntity.ok(fixtureService.postpone(id, request));
    }
}
