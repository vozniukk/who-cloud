package com.whocloud.auth.service;

import com.whocloud.auth.dto.*;
import com.whocloud.auth.entity.RefreshToken;
import com.whocloud.auth.entity.User;
import com.whocloud.auth.repository.RefreshTokenRepository;
import com.whocloud.auth.repository.UserRepository;
import com.whocloud.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // Generate tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();
            
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Revoke old refresh tokens
            refreshTokenRepository.revokeAllByUser(user);

            // Generate new tokens
            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = createRefreshToken(user);

            log.info("User logged in successfully: {}", user.getUsername());
            return buildAuthResponse(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token request received");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        
        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(user);
        
        log.info("Token refreshed successfully for user: {}", user.getUsername());
        return buildAuthResponse(user, newAccessToken, refreshToken.getToken());
    }

    public ValidateTokenResponse validateToken(String token) {
        log.debug("Validating token");

        try {
            if (!jwtUtil.validateToken(token)) {
                return ValidateTokenResponse.builder()
                        .valid(false)
                        .message("Invalid or expired token")
                        .build();
            }

            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ValidateTokenResponse.builder()
                    .valid(true)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(role)
                    .message("Token is valid")
                    .build();

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void logout(String username) {
        log.info("User logout: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        refreshTokenRepository.revokeAllByUser(user);
        log.info("User logged out successfully: {}", username);
    }

    private String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(30)) // 30 days expiration
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    public UserInfoResponse getUserInfo(String username) {
        log.debug("Getting user info for: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .googleId(user.getGoogleId())
                .emailVerified(user.getGoogleId() != null) // If has Google ID, email is verified
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .provider(user.getGoogleId() != null ? "Google" : "Local")
                .build();
    }
}
