package com.whocloud.publicweb.controller;

import com.whocloud.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/welcome")
    public ApiResponse<String> welcome() {
        return ApiResponse.success("Welcome to WHO Cloud Public Service");
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service is running");
    }
}
