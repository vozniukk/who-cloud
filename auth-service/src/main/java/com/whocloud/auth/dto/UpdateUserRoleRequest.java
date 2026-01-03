package com.whocloud.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRoleRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "GUEST|USER|MODERATOR|ADMIN", message = "Invalid role. Must be GUEST, USER, MODERATOR, or ADMIN")
    private String role;
}
