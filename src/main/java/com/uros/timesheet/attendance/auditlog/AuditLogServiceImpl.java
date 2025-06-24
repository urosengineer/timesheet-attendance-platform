package com.uros.timesheet.attendance.auditlog;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void log(String eventType, UUID userId, String details) {
        log(eventType, userId, details, null, null);
    }

    // New overload with IP and User-Agent
    @Transactional
    public void log(String eventType, UUID userId, String details, String ipAddress, String userAgent) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .user(user)
                .details(details)
                .createdAt(Instant.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogResponse> getLogsForUser(UUID userId) {
        return auditLogRepository.findByUserId(userId)
                .stream().map(auditLogMapper::toResponse).toList();
    }

    @Override
    public List<AuditLogResponse> getLogsByEventType(String eventType) {
        return auditLogRepository.findByEventType(eventType)
                .stream().map(auditLogMapper::toResponse).toList();
    }

    @Override
    public List<AuditLogResponse> getAll() {
        return auditLogRepository.findAll()
                .stream().map(auditLogMapper::toResponse).toList();
    }

    @Override
    public Page<AuditLogResponse> getLogs(int page, int size, String eventType, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);

        if (eventType != null && !eventType.isBlank() && userId != null) {
            return auditLogRepository.findByEventTypeAndUserId(eventType, userId, pageable)
                    .map(auditLogMapper::toResponse);
        }
        // Smart filtering logic: filters by eventType and/or userId
        if (eventType != null && !eventType.isBlank()) {
            return auditLogRepository.findByEventType(eventType, pageable)
                    .map(auditLogMapper::toResponse);
        } else if (userId != null) {
            return auditLogRepository.findByUserId(userId, pageable)
                    .map(auditLogMapper::toResponse);
        } else {
            return auditLogRepository.findAll(pageable)
                    .map(auditLogMapper::toResponse);
        }
    }
}