package com.ferwafa.auth;

import com.ferwafa.auth.dto.*;
import com.ferwafa.common.BusinessException;
import com.ferwafa.common.UserRole;
import com.ferwafa.config.JwtAuthenticationFilter.JwtUserDetails;
import com.ferwafa.config.JwtTokenProvider;
import com.ferwafa.config.SecurityUtils;
import com.ferwafa.referee.RefereeRepository;
import com.ferwafa.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RefereeRepository refereeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityUtils securityUtils;

    public AuthResponse login(LoginRequest request) {
        var admin = userRepository.findByUsername(request.getUsername());
        if (admin.isPresent()) {
            var user = admin.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            return buildResponse(user.getUsername(), user.getId(), UserRole.ADMIN.name(),
                    user.getFname() + " " + user.getLname());
        }

        var team = teamRepository.findByUsername(request.getUsername());
        if (team.isPresent()) {
            var t = team.get();
            if (!passwordEncoder.matches(request.getPassword(), t.getPasswordHash())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            return buildResponse(t.getUsername(), t.getTeamId(), UserRole.TEAM.name(), t.getName());
        }

        throw new BadCredentialsException("Invalid credentials");
    }

    public AuthResponse refereeLogin(RefereeLoginRequest request) {
        var referee = refereeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getAccessCode(), referee.getAccessCodeHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return buildResponse(referee.getEmail(), referee.getRefereeId(), UserRole.REFEREE.name(),
                referee.getFname() + " " + referee.getLname());
    }

    public MeResponse me() {
        JwtUserDetails user = securityUtils.currentUser();
        if (user == null) {
            throw new BusinessException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String displayName = resolveDisplayName(user);
        return MeResponse.builder()
                .role(user.role())
                .entityId(user.entityId())
                .username(user.username())
                .displayName(displayName)
                .build();
    }

    private String resolveDisplayName(JwtUserDetails user) {
        return switch (user.role()) {
            case "ADMIN" -> userRepository.findByUsername(user.username())
                    .map(u -> u.getFname() + " " + u.getLname()).orElse(user.username());
            case "TEAM" -> teamRepository.findByUsername(user.username())
                    .map(t -> t.getName()).orElse(user.username());
            case "REFEREE" -> refereeRepository.findByEmail(user.username())
                    .map(r -> r.getFname() + " " + r.getLname()).orElse(user.username());
            default -> user.username();
        };
    }

    private AuthResponse buildResponse(String subject, Long entityId, String role, String displayName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("entityId", entityId);
        String token = jwtTokenProvider.generateToken(subject, claims);
        return AuthResponse.builder()
                .token(token)
                .role(role)
                .entityId(entityId)
                .displayName(displayName)
                .build();
    }
}
