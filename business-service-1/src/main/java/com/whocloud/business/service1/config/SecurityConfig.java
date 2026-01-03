package com.whocloud.business.service1.config;

import com.whocloud.business.service1.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Actuator endpoints
                        .requestMatchers("/actuator/**").permitAll()
                        
                        // Public endpoints (if any)
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        
                        // Equipment endpoints - require authentication
                        .requestMatchers("/api/equipment/**").authenticated()
                        .requestMatchers("/api/custodians/**").authenticated()
                        .requestMatchers("/api/reports/**").authenticated()
                        
                        // Reference data endpoints - require authentication
                        .requestMatchers("/api/object-statuses/**").authenticated()
                        .requestMatchers("/api/custodian-statuses/**").authenticated()
                        .requestMatchers("/api/contract-types/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
