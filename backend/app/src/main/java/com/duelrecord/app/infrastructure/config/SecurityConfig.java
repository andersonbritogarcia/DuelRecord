package com.duelrecord.app.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.net.URI;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final List<String> allowedOrigins;

    public SecurityConfig(@Value("${app.cors.allowed-origins}") String origins) {
        this.allowedOrigins = Arrays.stream(origins.split(",")).map(String::trim).toList();
        if (allowedOrigins.isEmpty()) throw new IllegalArgumentException("At least one explicit CORS origin is required.");
        for (String origin : allowedOrigins) {
            URI uri = URI.create(origin);
            boolean local = uri.getHost() != null && List.of("localhost", "127.0.0.1", "[::1]").contains(uri.getHost());
            if (uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null
                    || uri.getFragment() != null || !uri.getPath().isEmpty()
                    || !("https".equals(uri.getScheme()) || (local && "http".equals(uri.getScheme())))) {
                throw new IllegalArgumentException("Configure explicit HTTPS CORS origins (HTTP is allowed only for loopback development).");
            }
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers(HttpMethod.GET, "/actuator/health", "/api/cards/**", "/api/geo/**").permitAll()
                        .requestMatchers("/api/players/me/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/players/ghost").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/players/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/decks/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/decks/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/matches/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/matches/**").authenticated()
                        .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Accept-Language"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
