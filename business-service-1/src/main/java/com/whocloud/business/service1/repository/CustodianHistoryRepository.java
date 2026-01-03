package com.whocloud.business.service1.repository;

import com.whocloud.business.service1.entity.CustodianHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustodianHistoryRepository extends JpaRepository<CustodianHistory, Long> {
    List<CustodianHistory> findByCustodianIdOrderByTimestampDesc(Long custodianId);
}
