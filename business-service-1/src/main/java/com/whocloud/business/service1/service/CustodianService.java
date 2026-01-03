package com.whocloud.business.service1.service;

import com.whocloud.business.service1.dto.custodian.CustodianRequest;
import com.whocloud.business.service1.dto.custodian.CustodianResponse;
import com.whocloud.business.service1.entity.*;
import com.whocloud.business.service1.exception.*;
import com.whocloud.business.service1.mapper.CustodianMapper;
import com.whocloud.business.service1.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustodianService {

    private final CustodianRepository custodianRepository;
    private final CustodianStatusRepository custodianStatusRepository;
    private final ContractTypeRepository contractTypeRepository;
    private final CustodianHistoryRepository custodianHistoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustodianMapper custodianMapper;

    public CustodianResponse createCustodian(CustodianRequest request, Long currentUserId, String ipAddress) {
        log.info("Creating custodian with identification number: {}", request.getIdentificationNumber());

        // Validate uniqueness
        validateUniqueIdentificationNumber(request.getIdentificationNumber(), null);
        if (request.getAuthUserId() != null) {
            validateUniqueAuthUserId(request.getAuthUserId(), null);
        }

        // Load references
        CustodianStatus status = custodianStatusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Custodian status not found with id: " + request.getStatusId()));
        ContractType contractType = contractTypeRepository.findById(request.getContractTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Contract type not found with id: " + request.getContractTypeId()));

        // Create entity
        Custodian custodian = custodianMapper.toEntity(request);
        custodian.setStatus(status);
        custodian.setContractType(contractType);

        custodian = custodianRepository.save(custodian);

        // Create history
        createHistoryRecord(custodian, "CREATED", "Custodian created", currentUserId, ipAddress);

        return custodianMapper.toResponse(custodian);
    }

    public CustodianResponse updateCustodian(Long id, CustodianRequest request, Long currentUserId, String ipAddress) {
        log.info("Updating custodian with id: {}", id);

        Custodian custodian = custodianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custodian not found with id: " + id));

        // Validate uniqueness
        validateUniqueIdentificationNumber(request.getIdentificationNumber(), id);
        if (request.getAuthUserId() != null) {
            validateUniqueAuthUserId(request.getAuthUserId(), id);
        }

        // Update basic fields
        custodianMapper.updateEntityFromRequest(custodian, request);

        // Update references if changed
        if (!custodian.getStatus().getId().equals(request.getStatusId())) {
            CustodianStatus status = custodianStatusRepository.findById(request.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Custodian status not found"));
            custodian.setStatus(status);
        }

        if (!custodian.getContractType().getId().equals(request.getContractTypeId())) {
            ContractType contractType = contractTypeRepository.findById(request.getContractTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contract type not found"));
            custodian.setContractType(contractType);
        }

        custodian = custodianRepository.save(custodian);
        createHistoryRecord(custodian, "UPDATED", "Custodian updated", currentUserId, ipAddress);

        return custodianMapper.toResponse(custodian);
    }

    @Transactional(readOnly = true)
    public CustodianResponse getCustodianById(Long id) {
        Custodian custodian = custodianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custodian not found with id: " + id));
        return custodianMapper.toResponse(custodian);
    }

    @Transactional(readOnly = true)
    public CustodianResponse getCustodianByAuthUserId(Long authUserId) {
        Custodian custodian = custodianRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Custodian not found for auth user id: " + authUserId));
        return custodianMapper.toResponse(custodian);
    }

    @Transactional(readOnly = true)
    public Page<CustodianResponse> getAllCustodians(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return custodianRepository.findAll(pageable)
                .map(custodianMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustodianResponse> getCustodiansByFilters(Long statusId, String position, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return custodianRepository.findByFilters(statusId, position, pageable)
                .map(custodianMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustodianResponse> searchCustodians(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return custodianRepository.searchCustodians(query, pageable)
                .map(custodianMapper::toResponse);
    }

    public void deactivateCustodian(Long id, Long currentUserId, String ipAddress) {
        log.info("Deactivating custodian with id: {}", id);

        Custodian custodian = custodianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custodian not found with id: " + id));

        CustodianStatus inactiveStatus = custodianStatusRepository.findByName("INACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("INACTIVE status not found"));

        custodian.setStatus(inactiveStatus);
        custodianRepository.save(custodian);

        // Move all equipment to warehouse
        List<Equipment> equipment = custodian.getEquipments();
        for (Equipment eq : equipment) {
            eq.setCurrentCustodian(null);
        }

        createHistoryRecord(custodian, "DEACTIVATED", "Custodian deactivated, equipment moved to warehouse", currentUserId, ipAddress);
    }

    // Private helper methods
    private void validateUniqueIdentificationNumber(String identificationNumber, Long excludeId) {
        custodianRepository.findByIdentificationNumber(identificationNumber)
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Custodian with identification number " + identificationNumber + " already exists");
                });
    }

    private void validateUniqueAuthUserId(Long authUserId, Long excludeId) {
        custodianRepository.findByAuthUserId(authUserId)
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Custodian is already linked to auth user id: " + authUserId);
                });
    }

    private void createHistoryRecord(Custodian custodian, String actionType, String details, Long userId, String ipAddress) {
        CustodianHistory history = CustodianHistory.builder()
                .custodian(custodian)
                .actionType(actionType)
                .details(details)
                .modifiedByUserId(userId)
                .ipAddress(ipAddress)
                .build();
        custodianHistoryRepository.save(history);
    }
}
