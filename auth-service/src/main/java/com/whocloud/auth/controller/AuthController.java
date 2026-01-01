package com.whocloud.auth.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<String> login(@RequestParam String username, @RequestParam String password) {
        // TODO: Implement JWT authentication
        return ApiResponse.success("Token generated successfully");
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestParam String username, @RequestParam String password) {
        // TODO: Implement user registration
        return ApiResponse.success("User registered successfully");
    }

    @GetMapping("/validate")
    public ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        // TODO: Implement token validation
        return ApiResponse.success(true);
    }
}
