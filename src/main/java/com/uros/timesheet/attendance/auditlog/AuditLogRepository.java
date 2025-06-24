package com.uros.timesheet.attendance.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserId(UUID userId);
    List<AuditLog> findByEventType(String eventType);

    Page<AuditLog> findByEventType(String eventType, Pageable pageable);
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    Page<AuditLog> findAll(Pageable pageable);
    Page<AuditLog> findByEventTypeAndUserId(String eventType, UUID userId, Pageable pageable);
}