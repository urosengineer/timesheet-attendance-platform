package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.workflow.WorkflowLogCreateRequest;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogResponse;

import java.util.List;
import java.util.UUID;

public interface WorkflowLogService {
    WorkflowLogResponse logTransition(WorkflowLogCreateRequest request);
    List<WorkflowLogResponse> getLogsForEntity(String entityType, UUID entityId);
}