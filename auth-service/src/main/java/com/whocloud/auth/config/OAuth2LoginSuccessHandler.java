package com.whocloud.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whocloud.auth.entity.RefreshToken;
import com.whocloud.auth.entity.User;
import com.whocloud.auth.repository.RefreshTokenRepository;
import com.whocloud.auth.repository.UserRepository;
import com.whocloud.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles successful OAuth2 authentication by generating JWT tokens
 * and returning them to the client.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        
        log.info("OAuth2 authentication successful for email: {}", email);
        
        // Find user by Google ID or email
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 authentication")));
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generate JWT tokens
        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = createRefreshToken(user);
        
        log.debug("Generated tokens for user: username={}, role={}", user.getUsername(), user.getRole());
        
        // Build response
        Map<String, Object> tokenResponse = new HashMap<>();
        tokenResponse.put("success", true);
        tokenResponse.put("accessToken", accessToken);
        tokenResponse.put("refreshToken", refreshToken);
        tokenResponse.put("tokenType", "Bearer");
        tokenResponse.put("expiresIn", 86400000); // 24 hours
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole().name());
        
        tokenResponse.put("user", userInfo);
        
        log.info("OAuth2 login completed successfully for user: {}", user.getUsername());
        
        // Redirect to frontend with token in URL parameter
        // Frontend will extract token and save it in localStorage + cookie
        String frontendUrl = "http://localhost:3000/auth/callback?token=" + accessToken;
        
        log.debug("Redirecting to frontend: {}", frontendUrl);
        response.sendRedirect(frontendUrl);
    }
    
    /**
     * Create refresh token for user
     */
    private String createRefreshToken(User user) {
        // Revoke all existing refresh tokens for this user
        refreshTokenRepository.revokeAllByUser(user);
        
        // Create new refresh token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        
        refreshTokenRepository.save(refreshToken);
        
        return token;
    }
}
