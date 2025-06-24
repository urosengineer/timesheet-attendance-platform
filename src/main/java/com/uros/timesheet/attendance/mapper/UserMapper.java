package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {OrganizationMapper.class, RoleMapper.class, TeamMapper.class})
public interface UserMapper {
    UserResponse toResponse(User entity);
}