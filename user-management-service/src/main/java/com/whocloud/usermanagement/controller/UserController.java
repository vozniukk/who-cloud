package com.whocloud.usermanagement.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping
    public ApiResponse<List<String>> getAllUsers() {
        return ApiResponse.success(List.of("user1", "user2", "user3"));
    }

    @GetMapping("/{id}")
    public ApiResponse<String> getUserById(@PathVariable String id) {
        return ApiResponse.success("User " + id);
    }

    @PostMapping
    public ApiResponse<String> createUser(@RequestParam String username) {
        return ApiResponse.success("User created: " + username);
    }

    @PutMapping("/{id}")
    public ApiResponse<String> updateUser(@PathVariable String id, @RequestParam String username) {
        return ApiResponse.success("User updated: " + id);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable String id) {
        return ApiResponse.success("User deleted: " + id);
    }
}
