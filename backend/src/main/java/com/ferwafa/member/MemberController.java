package com.ferwafa.member;

import com.ferwafa.member.dto.MemberRequest;
import com.ferwafa.member.dto.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/api/teams/{teamId}/members")
    @Operation(summary = "List team members")
    public ResponseEntity<List<MemberResponse>> list(@PathVariable Long teamId) {
        return ResponseEntity.ok(memberService.findByTeam(teamId));
    }

    @PostMapping("/api/teams/{teamId}/members")
    @Operation(summary = "Register a team member")
    public ResponseEntity<MemberResponse> create(@PathVariable Long teamId, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(teamId, request));
    }

    @PutMapping("/api/members/{id}")
    @Operation(summary = "Update a team member")
    public ResponseEntity<MemberResponse> update(@PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.update(id, request));
    }

    @DeleteMapping("/api/members/{id}")
    @Operation(summary = "Delete a team member")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
