package com.whocloud.publicweb.controller;

import com.whocloud.common.dto.ApiResponse;
import com.whocloud.publicweb.dto.DatabaseStats;
import com.whocloud.publicweb.service.DatabaseStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final DatabaseStatsService databaseStatsService;

    public PublicController(DatabaseStatsService databaseStatsService) {
        this.databaseStatsService = databaseStatsService;
    }

    @GetMapping("/welcome")
    public ApiResponse<String> welcome() {
        return ApiResponse.success("Welcome to WHO Cloud Public Service");
    }

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Service is running");
    }

    @GetMapping("/database-stats")
    public ApiResponse<DatabaseStats> getDatabaseStats() {
        DatabaseStats stats = databaseStatsService.getDatabaseStats();
        return ApiResponse.success(stats);
    }
}
