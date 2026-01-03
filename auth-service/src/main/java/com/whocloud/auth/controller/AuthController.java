package com.whocloud.auth.controller;

import com.whocloud.auth.dto.*;
import com.whocloud.auth.service.AuthService;
import com.whocloud.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request");
        
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<ValidateTokenResponse>> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        log.debug("Token validation request");
        
        ValidateTokenResponse response = authService.validateToken(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Logout request for user: {}", userDetails.getUsername());
        
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetails>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Get current user request");
        return ResponseEntity.ok(ApiResponse.success(userDetails));
    }

    @GetMapping("/user-info")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("Get user info request for: {}", userDetails.getUsername());
        
        UserInfoResponse userInfo = authService.getUserInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }
}
