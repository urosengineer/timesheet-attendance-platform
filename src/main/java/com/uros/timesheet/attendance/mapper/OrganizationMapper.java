package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.dto.organization.OrganizationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrganizationMapper {
    OrganizationResponse toResponse(Organization entity);
}