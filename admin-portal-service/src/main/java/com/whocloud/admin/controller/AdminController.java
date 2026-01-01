package com.whocloud.admin.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public ApiResponse<String> getDashboard() {
        return ApiResponse.success("Admin Dashboard Data");
    }

    @GetMapping("/stats")
    public ApiResponse<String> getStats() {
        return ApiResponse.success("System Statistics");
    }
}
