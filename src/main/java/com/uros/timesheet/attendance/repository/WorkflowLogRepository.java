package com.uros.timesheet.attendance.repository;

import com.uros.timesheet.attendance.domain.WorkflowLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkflowLogRepository extends JpaRepository<WorkflowLog, UUID> {
    List<WorkflowLog> findByRelatedEntityTypeAndRelatedEntityId(String relatedEntityType, UUID relatedEntityId);
}