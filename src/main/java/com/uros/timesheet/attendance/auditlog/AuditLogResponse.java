package com.uros.timesheet.attendance.auditlog;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AuditLogResponse {
    private UUID id;
    private String eventType;
    private UserResponse user;
    private String details;
    private Instant createdAt;
    private String ipAddress;
    private String userAgent;
}