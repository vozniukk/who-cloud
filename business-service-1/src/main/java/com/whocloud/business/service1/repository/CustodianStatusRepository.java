package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.CustodianStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustodianStatusRepository extends JpaRepository<CustodianStatus, Long> {
    Optional<CustodianStatus> findByName(String name);
}
