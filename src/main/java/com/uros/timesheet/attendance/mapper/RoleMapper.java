package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.dto.role.RoleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper {
    RoleResponse toResponse(Role entity);
}