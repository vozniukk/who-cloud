package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.Custodian;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustodianRepository extends JpaRepository<Custodian, Long> {

    Optional<Custodian> findByIdentificationNumber(String identificationNumber);

    /**
     * Find custodian by linked global auth-service User ID.
     * Example: findByAuthUserId(123) -> returns Custodian linked to kvoznjuk@gmail.com
     */
    Optional<Custodian> findByAuthUserId(Long authUserId);

    Page<Custodian> findByStatusId(Long statusId, Pageable pageable);

    Page<Custodian> findByPosition(String position, Pageable pageable);

    @Query("SELECT c FROM Custodian c WHERE " +
           "(:statusId IS NULL OR c.status.id = :statusId) AND " +
           "(:position IS NULL OR LOWER(c.position) = LOWER(:position))")
    Page<Custodian> findByFilters(@Param("statusId") Long statusId,
                                   @Param("position") String position,
                                   Pageable pageable);

    @Query("SELECT c FROM Custodian c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.identificationNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.position) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Custodian> searchCustodians(@Param("query") String query, Pageable pageable);

    long countByStatusId(Long statusId);
}
