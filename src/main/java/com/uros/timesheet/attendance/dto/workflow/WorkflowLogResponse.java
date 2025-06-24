package com.uros.timesheet.attendance.dto.workflow;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WorkflowLogResponse {
    private UUID id;
    private String relatedEntityType;
    private UUID relatedEntityId;
    private String oldStatus;
    private String newStatus;
    private UserResponse user;
    private Instant timestamp;
    private String comment;
}