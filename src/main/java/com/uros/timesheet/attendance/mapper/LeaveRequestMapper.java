package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, OrganizationMapper.class})
public interface LeaveRequestMapper {
    LeaveRequestResponse toResponse(LeaveRequest entity);
}