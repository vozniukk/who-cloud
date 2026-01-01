package com.whocloud.admin.repository;

import com.whocloud.admin.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByTargetUserOrderByCreatedAtDesc(String targetUser);
    
    List<AuditLog> findByPerformedByOrderByCreatedAtDesc(String performedBy);
    
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
