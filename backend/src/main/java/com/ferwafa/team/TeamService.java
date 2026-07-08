package com.ferwafa.team;

import com.ferwafa.common.BusinessException;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.team.dto.TeamRequest;
import com.ferwafa.team.dto.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return teamRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return toResponse(getTeam(id));
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        Team team = Team.builder()
                .name(request.getName())
                .logo(request.getLogo())
                .stadium(request.getStadium())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = getTeam(id);
        if (!team.getUsername().equals(request.getUsername()) && teamRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }
        team.setName(request.getName());
        team.setLogo(request.getLogo());
        team.setStadium(request.getStadium());
        team.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            team.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(teamRepository.save(team));
    }

    public Team getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Team not found", HttpStatus.NOT_FOUND));
    }

    /** Write access: admin or the owning team manager. */
    public void assertTeamAccess(Long teamId) {
        if (securityUtils.isAdmin()) return;
        if (securityUtils.isTeam() && teamId.equals(securityUtils.currentEntityId())) return;
        throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
    }

    /** Read access: admin, assigned referees (for match reports), or owning team. */
    public void assertTeamReadAccess(Long teamId) {
        if (securityUtils.isAdmin() || securityUtils.isReferee()) return;
        if (securityUtils.isTeam() && teamId.equals(securityUtils.currentEntityId())) return;
        throw new BusinessException("Access denied", HttpStatus.FORBIDDEN);
    }

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .name(team.getName())
                .logo(team.getLogo())
                .stadium(team.getStadium())
                .username(team.getUsername())
                .build();
    }
}
