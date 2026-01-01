package com.whocloud.auth.config;

import com.whocloud.auth.entity.User;
import com.whocloud.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom OAuth2 User Service for handling Google OAuth2 authentication.
 * Automatically registers new users with GUEST role on first login.
 */
@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load OAuth2 user from Google
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Extract user information from Google profile
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String givenName = oAuth2User.getAttribute("given_name");
        String familyName = oAuth2User.getAttribute("family_name");
        
        log.debug("OAuth2 user loaded: googleId={}, email={}, name={}", googleId, email, name);
        
        // Find or create user
        User user = findOrCreateUser(googleId, email, name, givenName, familyName);
        
        log.info("User authenticated via OAuth2: username={}, role={}", user.getUsername(), user.getRole());
        
        return oAuth2User;
    }
    
    /**
     * Find existing user or create new one with GUEST role
     */
    private User findOrCreateUser(String googleId, String email, String name, 
                                   String givenName, String familyName) {
        // Try to find by Google ID first
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        
        if (user != null) {
            log.debug("Existing user found by googleId: {}", googleId);
            return user;
        }
        
        // Try to find by email (user might have registered manually)
        user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            // Link Google ID to existing account
            log.info("Linking Google ID to existing user: {}", email);
            user.setGoogleId(googleId);
            return userRepository.save(user);
        }
        
        // Create new user with GUEST role
        log.info("Auto-registering new user with GUEST role: email={}", email);
        
        String username = generateUniqueUsername(email);
        String fullName = (name != null && !name.isBlank()) ? name : 
                          (givenName != null ? givenName + " " + familyName : email);
        
        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password (not used for OAuth)
                .fullName(fullName)
                .googleId(googleId)
                .role(User.Role.GUEST)  // Auto-registered users get GUEST role
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        
        return userRepository.save(newUser);
    }
    
    /**
     * Generate unique username from email
     */
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        // Ensure username is unique
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
