package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.WorkflowLog;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface WorkflowLogMapper {
    WorkflowLogResponse toResponse(WorkflowLog entity);
}