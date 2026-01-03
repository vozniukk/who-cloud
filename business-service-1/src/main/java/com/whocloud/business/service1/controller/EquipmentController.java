package com.whocloud.business.service1.controller;

import com.whocloud.business.service1.dto.equipment.EquipmentRequest;
import com.whocloud.business.service1.dto.equipment.EquipmentResponse;
import com.whocloud.business.service1.dto.equipment.TransferEquipmentRequest;
import com.whocloud.business.service1.dto.history.HistoryResponse;
import com.whocloud.business.service1.service.EquipmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<EquipmentResponse> createEquipment(
            @Valid @RequestBody EquipmentRequest request,
            HttpServletRequest httpRequest) {
        // TODO: Get actual user ID from security context
        Long currentUserId = 1L; // Placeholder
        String ipAddress = httpRequest.getRemoteAddr();
        
        EquipmentResponse response = equipmentService.createEquipment(request, currentUserId, ipAddress);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L; // Placeholder
        String ipAddress = httpRequest.getRemoteAddr();
        
        EquipmentResponse response = equipmentService.updateEquipment(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getEquipmentById(@PathVariable Long id) {
        EquipmentResponse response = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> getAllEquipment(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long custodianId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<EquipmentResponse> response;
        if (categoryId != null || statusId != null || custodianId != null) {
            response = equipmentService.getEquipmentByFilters(categoryId, statusId, custodianId, pageable);
        } else {
            response = equipmentService.getAllEquipment(pageable);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EquipmentResponse>> searchEquipment(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EquipmentResponse> response = equipmentService.searchEquipment(query, pageable);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<EquipmentResponse> transferEquipment(
            @PathVariable Long id,
            @Valid @RequestBody TransferEquipmentRequest request,
            HttpServletRequest httpRequest) {
        Long currentUserId = 1L; // Placeholder
        String ipAddress = httpRequest.getRemoteAddr();
        
        EquipmentResponse response = equipmentService.transferEquipment(id, request, currentUserId, ipAddress);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HistoryResponse>> getEquipmentHistory(@PathVariable Long id) {
        List<HistoryResponse> history = equipmentService.getEquipmentHistory(id);
        return ResponseEntity.ok(history);
    }
}
