package com.whocloud.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "GUEST|USER|MODERATOR|ADMIN", message = "Invalid role. Must be GUEST, USER, MODERATOR, or ADMIN")
    private String newRole;
}
