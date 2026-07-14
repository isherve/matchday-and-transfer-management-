package com.ferwafa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/health").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/standings", "/api/reports/standings/pdf",
                                "/api/reports/top-scorers", "/api/reports/cards-leaderboard",
                                "/api/transfer-windows", "/api/transfer-windows/open").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**", "/referee/**").permitAll()
                        .requestMatchers("/login", "/team/login", "/app", "/referee", "/referee/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/team/**").hasAnyRole("TEAM", "ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (wantsHtml(request)) {
                                response.sendRedirect("/login?error=login_required");
                            } else {
                                response.setStatus(401);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (wantsHtml(request)) {
                                response.sendRedirect("/login?error=access_denied");
                            } else {
                                response.setStatus(403);
                                response.setContentType("application/json");
                                response.getWriter().write(
                                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
                            }
                        })
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private boolean wantsHtml(jakarta.servlet.http.HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/")) {
            return false;
        }
        return accept == null || accept.contains("text/html");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Patterns only (avoid mixing with allowedOrigins — breaks Flutter web / Chrome)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://10.0.2.2:*",
                "https://localhost:*",
                "https://127.0.0.1:*",
                "https://*.onrender.com",
                "https://*.up.railway.app",
                "https://*.loca.lt",
                "https://*.trycloudflare.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
