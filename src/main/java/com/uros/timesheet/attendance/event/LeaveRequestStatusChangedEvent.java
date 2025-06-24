package com.uros.timesheet.attendance.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
public class LeaveRequestStatusChangedEvent extends ApplicationEvent {
    private final UUID leaveRequestId;
    private final UUID userId;
    private final String oldStatus;
    private final String newStatus;
    private final UUID changedByUserId;
    private final String reason;
    private final Instant changedAt;

    public LeaveRequestStatusChangedEvent(Object source, UUID leaveRequestId, UUID userId, String oldStatus, String newStatus, UUID changedByUserId, String reason, Instant changedAt) {
        super(source);
        this.leaveRequestId = leaveRequestId;
        this.userId = userId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.reason = reason;
        this.changedAt = changedAt;
    }
}