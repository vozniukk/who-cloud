package com.whocloud.business.service1.controller;

import com.whocloud.business.service1.dto.custodian.CustodianRequest;
import com.whocloud.business.service1.dto.custodian.CustodianResponse;
import com.whocloud.business.service1.service.CustodianService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/custodians")
@RequiredArgsConstructor
@Slf4j
public class CustodianController {

    private final CustodianService custodianService;

    @PostMapping
    public ResponseEntity<CustodianResponse> createCustodian(
            @Valid @RequestBody CustodianRequest request,
            HttpServletRequest httpRequest) {
        log.info("POST /api/custodians - Creating new custodian");
        
        // TODO: Get from SecurityContext after auth-service integration
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        
        CustodianResponse response = custodianService.createCustodian(request, currentUserId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustodianResponse> updateCustodian(
            @PathVariable Long id,
            @Valid @RequestBody CustodianRequest request,
            HttpServletRequest httpRequest) {
        log.info("PUT /api/custodians/{} - Updating custodian", id);
        
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        
        CustodianResponse response = custodianService.updateCustodian(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustodianResponse> getCustodianById(@PathVariable Long id) {
        log.info("GET /api/custodians/{} - Fetching custodian by id", id);
        CustodianResponse response = custodianService.getCustodianById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth-user/{authUserId}")
    public ResponseEntity<CustodianResponse> getCustodianByAuthUserId(@PathVariable Long authUserId) {
        log.info("GET /api/custodians/auth-user/{} - Fetching custodian by auth user id", authUserId);
        CustodianResponse response = custodianService.getCustodianByAuthUserId(authUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<CustodianResponse>> getCustodians(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("GET /api/custodians - Fetching custodians with filters");
        
        Page<CustodianResponse> response;
        if (statusId != null || position != null) {
            response = custodianService.getCustodiansByFilters(statusId, position, page, size, sortBy, sortDirection);
        } else {
            response = custodianService.getAllCustodians(page, size, sortBy, sortDirection);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustodianResponse>> searchCustodians(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("GET /api/custodians/search - Searching custodians with query: {}", query);
        Page<CustodianResponse> response = custodianService.searchCustodians(query, page, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateCustodian(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        log.info("DELETE /api/custodians/{} - Deactivating custodian", id);
        
        Long currentUserId = 1L;
        String ipAddress = httpRequest.getRemoteAddr();
        
        custodianService.deactivateCustodian(id, currentUserId, ipAddress);
        return ResponseEntity.noContent().build();
    }
}
