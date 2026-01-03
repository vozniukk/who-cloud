package com.whocloud.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String googleId;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String provider;
}
