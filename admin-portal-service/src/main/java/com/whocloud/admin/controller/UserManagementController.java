package com.whocloud.admin.controller;

import com.whocloud.admin.dto.AuditLogDto;
import com.whocloud.admin.dto.UpdateRoleRequest;
import com.whocloud.admin.dto.UserDto;
import com.whocloud.admin.service.UserManagementService;
import com.whocloud.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user management operations in admin portal
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        log.info("GET /admin/users - Get all users");
        List<UserDto> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get users by role
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<List<UserDto>>> getUsersByRole(@PathVariable String role) {
        log.info("GET /admin/users/role/{} - Get users by role", role);
        try {
            List<UserDto> users = userManagementService.getUsersByRole(role);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Get pending users (GUEST role)
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<UserDto>>> getPendingUsers() {
        log.info("GET /admin/users/pending - Get pending users");
        List<UserDto> users = userManagementService.getPendingUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        log.info("GET /admin/users/{} - Get user by ID", id);
        try {
            UserDto user = userManagementService.getUserById(id);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Update user role
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String adminUsername) {
        
        log.info("PUT /admin/users/{}/role - Update user role to {}", id, request.getNewRole());
        
        // Get admin username from header (set by API Gateway JWT filter)
        String admin = (adminUsername != null) ? adminUsername : "system";
        
        try {
            UserDto updatedUser = userManagementService.updateUserRole(
                    id, 
                    request.getNewRole(), 
                    admin
            );
            return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Enable/disable user
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserDto>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "X-User-Id", required = false) String adminUsername) {
        
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("'enabled' field is required", null));
        }
        
        log.info("PUT /admin/users/{}/status - Update user status to {}", id, enabled);
        
        String admin = (adminUsername != null) ? adminUsername : "system";
        
        try {
            UserDto updatedUser = userManagementService.updateUserStatus(id, enabled, admin);
            return ResponseEntity.ok(ApiResponse.success(
                    "User status updated successfully", 
                    updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Get audit logs for a user
     */
    @GetMapping("/{username}/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getUserAuditLogs(@PathVariable String username) {
        log.info("GET /admin/users/{}/audit-logs - Get audit logs for user", username);
        List<AuditLogDto> auditLogs = userManagementService.getUserAuditLogs(username);
        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    /**
     * Get all audit logs
     */
    @GetMapping("/audit-logs/all")
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAllAuditLogs() {
        log.info("GET /admin/users/audit-logs/all - Get all audit logs");
        List<AuditLogDto> auditLogs = userManagementService.getAllAuditLogs();
        return ResponseEntity.ok(ApiResponse.success(auditLogs));
    }

    /**
     * Update user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserDto updateRequest,
            @RequestHeader(value = "X-User-Id", required = false) String adminUsername) {
        
        log.info("PUT /admin/users/{} - Update user profile", id);
        
        String admin = (adminUsername != null) ? adminUsername : "system";
        
        try {
            UserDto updatedUser = userManagementService.updateUserProfile(id, updateRequest, admin);
            return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Delete user (soft delete - disable account)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String adminUsername) {
        
        log.info("DELETE /admin/users/{} - Delete user", id);
        
        String admin = (adminUsername != null) ? adminUsername : "system";
        
        try {
            userManagementService.deleteUser(id, admin);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully (account disabled)", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Permanently delete user from database (use with caution!)
     */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String adminUsername) {
        
        log.warn("DELETE /admin/users/{}/permanent - PERMANENTLY delete user", id);
        
        String admin = (adminUsername != null) ? adminUsername : "system";
        
        try {
            userManagementService.permanentlyDeleteUser(id, admin);
            return ResponseEntity.ok(ApiResponse.success("User PERMANENTLY deleted from database", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        log.info("GET /admin/users/stats - Get user statistics");
        
        List<UserDto> allUsers = userManagementService.getAllUsers();
        
        long totalUsers = allUsers.size();
        long guestUsers = allUsers.stream().filter(u -> "GUEST".equals(u.getRole())).count();
        long regularUsers = allUsers.stream().filter(u -> "USER".equals(u.getRole())).count();
        long moderators = allUsers.stream().filter(u -> "MODERATOR".equals(u.getRole())).count();
        long admins = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long activeUsers = allUsers.stream().filter(UserDto::isEnabled).count();
        long disabledUsers = totalUsers - activeUsers;
        
        Map<String, Object> stats = Map.of(
                "totalUsers", totalUsers,
                "activeUsers", activeUsers,
                "disabledUsers", disabledUsers,
                "roleBreakdown", Map.of(
                        "GUEST", guestUsers,
                        "USER", regularUsers,
                        "MODERATOR", moderators,
                        "ADMIN", admins
                )
        );
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

