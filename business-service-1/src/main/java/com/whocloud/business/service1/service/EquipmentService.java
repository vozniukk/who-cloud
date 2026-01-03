package com.whocloud.business.service1.service;

import com.whocloud.business.service1.dto.equipment.EquipmentRequest;
import com.whocloud.business.service1.dto.equipment.EquipmentResponse;
import com.whocloud.business.service1.dto.equipment.TransferEquipmentRequest;
import com.whocloud.business.service1.dto.history.HistoryResponse;
import com.whocloud.business.service1.entity.*;
import com.whocloud.business.service1.exception.BusinessLogicException;
import com.whocloud.business.service1.exception.DuplicateResourceException;
import com.whocloud.business.service1.exception.ResourceNotFoundException;
import com.whocloud.business.service1.mapper.EquipmentMapper;
import com.whocloud.business.service1.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectStatusRepository objectStatusRepository;
    private final CustodianRepository custodianRepository;
    private final EquipmentHistoryRepository equipmentHistoryRepository;
    private final EquipmentMapper equipmentMapper;

    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request, Long currentUserId, String ipAddress) {
        log.info("Creating equipment with inventory number: {}", request.getInventoryNumber());

        // Validate uniqueness
        validateUniqueInventoryNumber(request.getInventoryNumber(), null);
        if (request.getSerialNumber() != null) {
            validateUniqueSerialNumber(request.getSerialNumber(), null);
        }

        // Validate dates
        validateDates(request);

        // Load references
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        ObjectStatus status = objectStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with id: " + request.getStatusId()));

        Custodian owner = null;
        if (request.getCurrentCustodianId() != null) {
            owner = custodianRepository.findById(request.getCurrentCustodianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Custodian not found with id: " + request.getCurrentCustodianId()));
        }

        // Create equipment
        Equipment equipment = equipmentMapper.toEntity(request);
        equipment.setCategory(category);
        equipment.setStatus(status);
        equipment.setCurrentCustodian(owner);

        Equipment saved = equipmentRepository.save(equipment);

        // Create history record
        createHistoryRecord(saved, "CREATED", null, owner != null ? owner.getId() : null,
                "Equipment created", currentUserId, ipAddress);

        log.info("Equipment created successfully with id: {}", saved.getId());
        return equipmentMapper.toResponse(saved);
    }

    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentRequest request, Long currentUserId, String ipAddress) {
        log.info("Updating equipment with id: {}", id);

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));

        // Validate uniqueness (excluding current equipment)
        validateUniqueInventoryNumber(request.getInventoryNumber(), id);
        if (request.getSerialNumber() != null) {
            validateUniqueSerialNumber(request.getSerialNumber(), id);
        }

        // Validate dates
        validateDates(request);

        // Load new references if changed
        if (!equipment.getCategory().getId().equals(request.getCategoryId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            equipment.setCategory(category);
        }

        if (!equipment.getStatus().getId().equals(request.getStatusId())) {
            ObjectStatus status = objectStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Status not found with id: " + request.getStatusId()));
            equipment.setStatus(status);
        }

        // Update basic fields
        equipmentMapper.updateEntityFromRequest(equipment, request);

        Equipment updated = equipmentRepository.save(equipment);

        // Create history record
        createHistoryRecord(updated, "UPDATED", null, null,
                "Equipment updated", currentUserId, ipAddress);

        log.info("Equipment updated successfully with id: {}", id);
        return equipmentMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        return equipmentMapper.toResponse(equipment);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentResponse> getAllEquipment(Pageable pageable) {
        return equipmentRepository.findAll(pageable)
                .map(equipmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentResponse> getEquipmentByFilters(Long categoryId, Long statusId, Long ownerId, Pageable pageable) {
        return equipmentRepository.findByFilters(categoryId, statusId, ownerId, pageable)
                .map(equipmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EquipmentResponse> searchEquipment(String query, Pageable pageable) {
        return equipmentRepository.searchEquipment(query, pageable)
                .map(equipmentMapper::toResponse);
    }

    @Transactional
    public EquipmentResponse transferEquipment(Long equipmentId, TransferEquipmentRequest request,
                                               Long currentUserId, String ipAddress) {
        log.info("Transferring equipment id: {} to Custodian id: {}", equipmentId, request.getToCustodianId());

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + equipmentId));

        Long fromCustodianId = equipment.getCurrentCustodian() != null ? equipment.getCurrentCustodian().getId() : null;
        Long toCustodianId = request.getToCustodianId();

        Custodian newOwner = null;
        if (toCustodianId != null) {
            newOwner = custodianRepository.findById(toCustodianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Custodian not found with id: " + toCustodianId));
            
            // Check if Custodian is active
            CustodianStatus activeStatus = custodianRepository.findById(toCustodianId)
                    .map(Custodian::getStatus)
                    .orElse(null);
            if (activeStatus != null && "INACTIVE".equalsIgnoreCase(activeStatus.getName())) {
                throw new BusinessLogicException("Cannot transfer equipment to inactive Custodian");
            }
        }

        equipment.setCurrentCustodian(newOwner);
        Equipment transferred = equipmentRepository.save(equipment);

        // Create history record
        createHistoryRecord(transferred, "TRANSFERRED", fromCustodianId, toCustodianId,
                request.getDetails(), currentUserId, ipAddress);

        log.info("Equipment transferred successfully");
        return equipmentMapper.toResponse(transferred);
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> getEquipmentHistory(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment not found with id: " + equipmentId);
        }

        List<EquipmentHistory> history = equipmentHistoryRepository.findByEquipmentIdOrderByTimestampDesc(equipmentId);

        return history.stream()
                .map(this::mapHistoryToResponse)
                .toList();
    }

    private void validateUniqueInventoryNumber(String inventoryNumber, Long excludeId) {
        equipmentRepository.findByInventoryNumber(inventoryNumber)
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        throw new DuplicateResourceException("Inventory number already exists: " + inventoryNumber);
                    }
                });
    }

    private void validateUniqueSerialNumber(String serialNumber, Long excludeId) {
        equipmentRepository.findBySerialNumber(serialNumber)
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        throw new DuplicateResourceException("Serial number already exists: " + serialNumber);
                    }
                });
    }

    private void validateDates(EquipmentRequest request) {
        if (request.getCommissioningDate().isBefore(request.getReleaseDate())) {
            throw new BusinessLogicException("Commissioning date cannot be before release date");
        }
    }

    private void createHistoryRecord(Equipment equipment, String actionType,
                                      Long fromCustodianId, Long toCustodianId,
                                      String details, Long userId, String ipAddress) {
        EquipmentHistory history = EquipmentHistory.builder()
                .equipment(equipment)
                .actionType(actionType)
                .timestamp(LocalDateTime.now())
                .fromCustodianId(fromCustodianId)
                .toCustodianId(toCustodianId)
                .details(details)
                .userId(userId)
                .ipAddress(ipAddress)
                .build();

        equipmentHistoryRepository.save(history);
    }

    private HistoryResponse mapHistoryToResponse(EquipmentHistory history) {
        String fromCustodianName = history.getFromCustodianId() != null
                ? custodianRepository.findById(history.getFromCustodianId()).map(Custodian::getFullName).orElse("Unknown")
                : "Warehouse";

        String toCustodianName = history.getToCustodianId() != null
                ? custodianRepository.findById(history.getToCustodianId()).map(Custodian::getFullName).orElse("Unknown")
                : "Warehouse";

        return HistoryResponse.builder()
                .id(history.getId())
                .actionType(history.getActionType())
                .timestamp(history.getTimestamp())
                .fromCustodianId(history.getFromCustodianId())
                .fromCustodianName(fromCustodianName)
                .toCustodianId(history.getToCustodianId())
                .toCustodianName(toCustodianName)
                .details(history.getDetails())
                .userId(history.getUserId())
                .ipAddress(history.getIpAddress())
                .build();
    }
}
