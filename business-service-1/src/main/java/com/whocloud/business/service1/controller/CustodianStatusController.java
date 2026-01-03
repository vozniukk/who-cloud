package com.whocloud.business.service1.controller;

import com.whocloud.business.service1.entity.CustodianStatus;
import com.whocloud.business.service1.repository.CustodianStatusRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/custodian-statuses")
@RequiredArgsConstructor
@Slf4j
public class CustodianStatusController {

    private final CustodianStatusRepository custodianStatusRepository;

    @GetMapping
    public ResponseEntity<List<CustodianStatus>> getAllCustodianStatuses() {
        log.info("GET /custodian-statuses - Fetching all custodian statuses");
        List<CustodianStatus> statuses = custodianStatusRepository.findAll();
        return ResponseEntity.ok(statuses);
    }

    @PostMapping
    public ResponseEntity<CustodianStatus> createCustodianStatus(@Valid @RequestBody CustodianStatus custodianStatus) {
        log.info("POST /custodian-statuses - Creating new custodian status: {}", custodianStatus.getName());
        CustodianStatus saved = custodianStatusRepository.save(custodianStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustodianStatus> updateCustodianStatus(@PathVariable Long id, @Valid @RequestBody CustodianStatus custodianStatus) {
        log.info("PUT /custodian-statuses/{} - Updating custodian status", id);
        return custodianStatusRepository.findById(id)
                .map(existing -> {
                    existing.setName(custodianStatus.getName());
                    existing.setTranslations(custodianStatus.getTranslations());
                    CustodianStatus updated = custodianStatusRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustodianStatus(@PathVariable Long id) {
        log.info("DELETE /custodian-statuses/{} - Deleting custodian status", id);
        if (custodianStatusRepository.existsById(id)) {
            custodianStatusRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
