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
        
        // For browser-based OAuth2 flow, redirect to a success page with tokens
        // Option 1: Return HTML page with token (for demo/testing)
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Login Successful</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; background: #f5f5f5; }
                    .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); max-width: 800px; margin: 0 auto; }
                    h1 { color: #4CAF50; }
                    .token-box { background: #f9f9f9; padding: 15px; border: 1px solid #ddd; border-radius: 4px; margin: 15px 0; overflow-wrap: break-word; }
                    .label { font-weight: bold; color: #333; margin-top: 15px; }
                    .user-info { background: #e3f2fd; padding: 15px; border-radius: 4px; margin: 15px 0; }
                    .role-badge { display: inline-block; padding: 5px 10px; background: #ff9800; color: white; border-radius: 4px; font-weight: bold; }
                    .copy-btn { background: #2196F3; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; margin-top: 10px; }
                    .copy-btn:hover { background: #1976D2; }
                    code { font-family: 'Courier New', monospace; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>✓ Authentication Successful!</h1>
                    
                    <div class="user-info">
                        <div class="label">User Information:</div>
                        <p><strong>Username:</strong> %s</p>
                        <p><strong>Email:</strong> %s</p>
                        <p><strong>Full Name:</strong> %s</p>
                        <p><strong>Role:</strong> <span class="role-badge">%s</span></p>
                    </div>
                    
                    <div class="label">Access Token (JWT):</div>
                    <div class="token-box">
                        <code id="accessToken">%s</code>
                        <button class="copy-btn" onclick="copyToken('accessToken')">Copy Access Token</button>
                    </div>
                    
                    <div class="label">Refresh Token:</div>
                    <div class="token-box">
                        <code id="refreshToken">%s</code>
                        <button class="copy-btn" onclick="copyToken('refreshToken')">Copy Refresh Token</button>
                    </div>
                    
                    <div class="label">Token Type:</div>
                    <p>Bearer</p>
                    
                    <div class="label">Expires In:</div>
                    <p>24 hours (86400000 ms)</p>
                    
                    <div class="label">Next Steps:</div>
                    <p>1. Copy the Access Token above</p>
                    <p>2. Test GUEST access to information-service-a:</p>
                    <div class="token-box">
                        <code>curl http://localhost:8080/api/info-a/ -H "Authorization: Bearer YOUR_TOKEN"</code>
                    </div>
                    <p>3. Try accessing business-service-1 (should fail with 403 - GUEST role insufficient):</p>
                    <div class="token-box">
                        <code>curl http://localhost:8080/api/business-1/ -H "Authorization: Bearer YOUR_TOKEN"</code>
                    </div>
                </div>
                
                <script>
                    function copyToken(elementId) {
                        const tokenText = document.getElementById(elementId).textContent;
                        navigator.clipboard.writeText(tokenText).then(() => {
                            alert('Token copied to clipboard!');
                        });
                    }
                </script>
            </body>
            </html>
            """.formatted(
                user.getUsername(),
                user.getEmail(),
                user.getFullName() != null ? user.getFullName() : "N/A",
                user.getRole().name(),
                accessToken,
                refreshToken
            );
        
        response.getWriter().write(html);
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
