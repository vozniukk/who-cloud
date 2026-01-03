package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.ContractType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {
    
    Optional<ContractType> findByName(String name);
    
    boolean existsByName(String name);
}
