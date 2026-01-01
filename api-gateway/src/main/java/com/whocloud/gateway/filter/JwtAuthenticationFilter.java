package com.whocloud.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * JWT Authentication Filter for API Gateway.
 * Validates JWT tokens and enforces role-based access control.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            log.debug("JWT filter processing request: path={}, requiredRoles={}", 
                     request.getPath(), config.getRequiredRoles());
            
            // Extract token from Authorization header
            String token = extractToken(request);
            
            if (token == null || token.isEmpty()) {
                log.warn("Missing or invalid Authorization header for path: {}", request.getPath());
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            
            try {
                // Validate token and extract claims
                Claims claims = validateToken(token);
                
                // Extract user information
                String username = claims.getSubject();
                String role = claims.get("role", String.class);
                
                log.debug("Token validated: username={}, role={}", username, role);
                
                // Check if user has required role
                if (!hasRequiredRole(role, config.getRequiredRoles())) {
                    log.warn("Access denied for user {} with role {} to path {}. Required roles: {}", 
                            username, role, request.getPath(), config.getRequiredRoles());
                    return onError(exchange, "Insufficient permissions", HttpStatus.FORBIDDEN);
                }
                
                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", username)
                        .header("X-User-Role", role)
                        .build();
                
                log.info("Access granted for user {} with role {} to path {}", 
                        username, role, request.getPath());
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }
    
    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        
        String authHeader = authHeaders.get(0);
        
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
    
    /**
     * Validate JWT token and extract claims
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if user role matches any of the required roles
     */
    private boolean hasRequiredRole(String userRole, List<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true; // No specific role required
        }
        
        return requiredRoles.stream()
                .anyMatch(requiredRole -> requiredRole.equalsIgnoreCase(userRole));
    }
    
    /**
     * Return error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorJson = String.format(
                "{\"success\":false,\"error\":\"%s\",\"timestamp\":\"%s\"}",
                message,
                new Date()
        );
        
        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8)))
        );
    }
    
    /**
     * Configuration class for filter
     */
    public static class Config {
        private List<String> requiredRoles;
        
        public List<String> getRequiredRoles() {
            return requiredRoles;
        }
        
        public void setRequiredRoles(String roles) {
            this.requiredRoles = Arrays.asList(roles.split(","));
        }
    }
}
