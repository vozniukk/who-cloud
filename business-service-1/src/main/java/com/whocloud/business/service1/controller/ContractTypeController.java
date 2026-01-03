package com.whocloud.business.service1.controller;

import com.whocloud.business.service1.entity.ContractType;
import com.whocloud.business.service1.repository.ContractTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contract-types")
@RequiredArgsConstructor
@Slf4j
public class ContractTypeController {

    private final ContractTypeRepository contractTypeRepository;

    @GetMapping
    public ResponseEntity<List<ContractType>> getAllContractTypes() {
        log.info("GET /contract-types - Fetching all contract types");
        List<ContractType> contractTypes = contractTypeRepository.findAll();
        return ResponseEntity.ok(contractTypes);
    }

    @PostMapping
    public ResponseEntity<ContractType> createContractType(@Valid @RequestBody ContractType contractType) {
        log.info("POST /contract-types - Creating new contract type: {}", contractType.getName());
        ContractType saved = contractTypeRepository.save(contractType);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContractType> updateContractType(@PathVariable Long id, @Valid @RequestBody ContractType contractType) {
        log.info("PUT /contract-types/{} - Updating contract type", id);
        return contractTypeRepository.findById(id)
                .map(existing -> {
                    existing.setName(contractType.getName());
                    existing.setTranslations(contractType.getTranslations());
                    ContractType updated = contractTypeRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContractType(@PathVariable Long id) {
        log.info("DELETE /contract-types/{} - Deleting contract type", id);
        if (contractTypeRepository.existsById(id)) {
            contractTypeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
