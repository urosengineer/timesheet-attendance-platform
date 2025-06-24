package com.uros.timesheet.attendance.dto.workflow;

import lombok.Data;
import lombok.Builder;
import java.util.UUID;

@Data
@Builder
public class WorkflowLogCreateRequest {
    private String relatedEntityType;
    private UUID relatedEntityId;
    private String oldStatus;
    private String newStatus;
    private UUID userId;
    private String comment;
}