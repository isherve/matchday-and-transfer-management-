package com.ferwafa.config;

import com.ferwafa.config.JwtAuthenticationFilter.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public JwtUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof JwtUserDetails details) {
            return details;
        }
        return null;
    }

    public Long currentEntityId() {
        JwtUserDetails user = currentUser();
        return user != null ? user.entityId() : null;
    }

    public String currentRole() {
        JwtUserDetails user = currentUser();
        return user != null ? user.role() : null;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(currentRole());
    }

    public boolean isTeam() {
        return "TEAM".equals(currentRole());
    }

    public boolean isReferee() {
        return "REFEREE".equals(currentRole());
    }
}
