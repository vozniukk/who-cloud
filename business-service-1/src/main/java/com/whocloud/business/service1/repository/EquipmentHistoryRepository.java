package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.EquipmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentHistoryRepository extends JpaRepository<EquipmentHistory, Long>, JpaSpecificationExecutor<EquipmentHistory> {
    
    List<EquipmentHistory> findByEquipmentIdOrderByTimestampDesc(Long equipmentId);
    
    Page<EquipmentHistory> findByEquipmentId(Long equipmentId, Pageable pageable);
}
