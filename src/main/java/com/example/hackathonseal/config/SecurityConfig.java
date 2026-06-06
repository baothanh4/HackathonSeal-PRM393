package com.example.hackathonseal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // For development allow all. For production, restrict origins and credentials appropriately.
        configuration.setAllowedOriginPatterns(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
