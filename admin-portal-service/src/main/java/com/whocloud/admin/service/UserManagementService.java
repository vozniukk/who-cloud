package com.whocloud.admin.service;

import com.whocloud.admin.dto.AuditLogDto;
import com.whocloud.admin.dto.UserDto;
import com.whocloud.admin.entity.AuditLog;
import com.whocloud.admin.entity.User;
import com.whocloud.admin.repository.AuditLogRepository;
import com.whocloud.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing users and administrative actions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Get all users
     */
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get users by role
     */
    public List<UserDto> getUsersByRole(String role) {
        log.debug("Fetching users with role: {}", role);
        User.Role roleEnum = User.Role.valueOf(role.toUpperCase());
        return userRepository.findByRole(roleEnum).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pending users (GUEST role)
     */
    public List<UserDto> getPendingUsers() {
        log.debug("Fetching pending users (GUEST role)");
        return userRepository.findPendingUsers().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserDto getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        return convertToDto(user);
    }

    /**
     * Update user role
     */
    @Transactional
    public UserDto updateUserRole(Long userId, String newRole, String adminUsername) {
        log.info("Updating user role: userId={}, newRole={}, admin={}", userId, newRole, adminUsername);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        String oldRole = user.getRole().name();

        // Validate role change
        if (oldRole.equals(newRole)) {
            throw new IllegalArgumentException("User already has role: " + newRole);
        }

        // Update role
        user.setRole(User.Role.valueOf(newRole.toUpperCase()));
        User updatedUser = userRepository.save(user);

        // Create audit log
        createAuditLog(
                "ROLE_UPDATE",
                adminUsername,
                user.getUsername(),
                oldRole,
                newRole,
                String.format("User role updated from %s to %s", oldRole, newRole)
        );

        log.info("User role updated successfully: username={}, oldRole={}, newRole={}", 
                user.getUsername(), oldRole, newRole);

        return convertToDto(updatedUser);
    }

    /**
     * Enable/disable user
     */
    @Transactional
    public UserDto updateUserStatus(Long userId, boolean enabled, String adminUsername) {
        log.info("Updating user status: userId={}, enabled={}, admin={}", userId, enabled, adminUsername);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        boolean oldStatus = user.isEnabled();
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        // Create audit log
        createAuditLog(
                "STATUS_UPDATE",
                adminUsername,
                user.getUsername(),
                String.valueOf(oldStatus),
                String.valueOf(enabled),
                String.format("User account %s", enabled ? "enabled" : "disabled")
        );

        log.info("User status updated successfully: username={}, enabled={}", user.getUsername(), enabled);

        return convertToDto(updatedUser);
    }

    /**
     * Get audit logs for a user
     */
    public List<AuditLogDto> getUserAuditLogs(String username) {
        log.debug("Fetching audit logs for user: {}", username);
        return auditLogRepository.findByTargetUserOrderByCreatedAtDesc(username).stream()
                .map(this::convertAuditLogToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all audit logs
     */
    public List<AuditLogDto> getAllAuditLogs() {
        log.debug("Fetching all audit logs");
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::convertAuditLogToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create audit log entry
     */
    private void createAuditLog(String action, String performedBy, String targetUser,
                                String oldValue, String newValue, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .targetUser(targetUser)
                .oldValue(oldValue)
                .newValue(newValue)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, performedBy={}, targetUser={}", action, performedBy, targetUser);
    }

    /**
     * Convert User entity to DTO
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .googleId(user.getGoogleId())
                .role(user.getRole().name())
                .isEnabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Convert AuditLog entity to DTO
     */
    private AuditLogDto convertAuditLogToDto(AuditLog auditLog) {
        return AuditLogDto.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .performedBy(auditLog.getPerformedBy())
                .targetUser(auditLog.getTargetUser())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .details(auditLog.getDetails())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
