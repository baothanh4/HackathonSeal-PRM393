package com.example.hackathonseal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public API endpoints (registration, login)
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Swagger / OpenAPI - allow access to API docs and UI
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()
                        // Event endpoints - GET is open to all authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/**").authenticated()
                        // Event registration (authenticated users to register themselves or guests)
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/*/register").authenticated()
                        // Event list participants (admin/coordinator only)
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/*/registrations").hasAnyRole("ADMIN", "COORDINATOR")
                        // Event write operations require ADMIN role (create/update/delete events)
                        .requestMatchers(HttpMethod.POST, "/api/v1/events").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")
                        // Unregister requires ADMIN or owner (via @PreAuthorize in controller)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/*/registrations/**").authenticated()
                        // Team management within events (authenticated users)
                        .requestMatchers("/api/v1/events/*/teams/**").authenticated()
                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
