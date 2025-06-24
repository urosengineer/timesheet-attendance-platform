package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.Permission;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission entity);
}