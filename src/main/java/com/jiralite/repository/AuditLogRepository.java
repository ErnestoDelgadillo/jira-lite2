package com.jiralite.repository;

import com.jiralite.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByProjectIdOrderByTimestampDesc(Long projectId);
    List<AuditLog> findByTaskIdOrderByTimestampDesc(Long taskId);
}

