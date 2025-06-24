package com.uros.timesheet.attendance.auditlog;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {com.uros.timesheet.attendance.mapper.UserMapper.class})
public interface AuditLogMapper {
    AuditLogResponse toResponse(AuditLog entity);
}