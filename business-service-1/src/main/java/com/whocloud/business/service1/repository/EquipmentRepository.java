package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    Optional<Equipment> findByInventoryNumber(String inventoryNumber);

    Optional<Equipment> findBySerialNumber(String serialNumber);

    List<Equipment> findByCurrentCustodianId(Long custodianId);

    Page<Equipment> findByCurrentCustodianId(Long custodianId, Pageable pageable);

    Page<Equipment> findByStatusId(Long statusId, Pageable pageable);

    Page<Equipment> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Equipment> findByCurrentCustodianIsNull(Pageable pageable); // В warehouse

    @Query("SELECT e FROM Equipment e WHERE " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:statusId IS NULL OR e.status.id = :statusId) AND " +
           "(:custodianId IS NULL OR e.currentCustodian.id = :custodianId)")
    Page<Equipment> findByFilters(@Param("categoryId") Long categoryId,
                                   @Param("statusId") Long statusId,
                                   @Param("custodianId") Long custodianId,
                                   Pageable pageable);

    @Query("SELECT e FROM Equipment e WHERE " +
           "LOWER(e.inventoryNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.serialNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.manufacturer) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.category.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Equipment> searchEquipment(@Param("query") String query, Pageable pageable);

    long countByStatusId(Long statusId);

    long countByCategoryId(Long categoryId);

    long countByCurrentCustodianIsNull(); // Count in warehouse
}
