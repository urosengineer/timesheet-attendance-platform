package com.uros.timesheet.attendance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class AttendanceRecordStatusChangedEvent extends ApplicationEvent {
    private final UUID attendanceRecordId;
    private final UUID userId;
    private final String oldStatus;
    private final String newStatus;
    private final UUID changedByUserId;
    private final String reason;
    private final Instant changedAt;

    public AttendanceRecordStatusChangedEvent(Object source, UUID attendanceRecordId, UUID userId, String oldStatus, String newStatus, UUID changedByUserId, String reason, Instant changedAt) {
        super(source);
        this.attendanceRecordId = attendanceRecordId;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
        this.changedAt = changedAt;
    }
}