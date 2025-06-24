package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.Team;
import com.uros.timesheet.attendance.dto.team.TeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    TeamResponse toResponse(Team entity);
}