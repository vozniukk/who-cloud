package com.whocloud.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Enabled status is required")
    private Boolean enabled;
}
