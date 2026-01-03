package com.whocloud.auth.dto;

import com.whocloud.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String googleId;
    private String role;
    private boolean isEnabled;
    private boolean isAccountNonLocked;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String provider;

    public static UserManagementResponse fromUser(User user) {
        return UserManagementResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .googleId(user.getGoogleId())
                .role(user.getRole().name())
                .isEnabled(user.isEnabled())
                .isAccountNonLocked(user.isAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .provider(user.getGoogleId() != null ? "Google" : "Local")
                .build();
    }
}
