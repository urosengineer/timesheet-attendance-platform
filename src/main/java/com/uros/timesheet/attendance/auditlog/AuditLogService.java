package com.uros.timesheet.attendance.auditlog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AuditLogService {
    void log(String eventType, UUID userId, String details);
    List<AuditLogResponse> getLogsForUser(UUID userId);
    List<AuditLogResponse> getLogsByEventType(String eventType);
    List<AuditLogResponse> getAll();
    Page<AuditLogResponse> getLogs(int page, int size, String eventType, UUID userId);
}