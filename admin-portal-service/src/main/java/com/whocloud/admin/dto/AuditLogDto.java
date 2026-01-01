package com.whocloud.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private String action;
    private String performedBy;
    private String targetUser;
    private String oldValue;
    private String newValue;
    private String details;
    private LocalDateTime createdAt;
}
