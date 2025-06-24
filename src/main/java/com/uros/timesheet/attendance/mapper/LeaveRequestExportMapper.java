package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.dto.export.LeaveRequestExportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LeaveRequestExportMapper {
    @Mapping(target = "startDate", expression = "java(entity.getStartDate().format(java.time.format.DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")))")
    @Mapping(target = "endDate", expression = "java(entity.getEndDate().format(java.time.format.DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")))")
    @Mapping(target = "approver", expression = "java(entity.getApprover() != null ? entity.getApprover().getFullName() : \"\")")
    LeaveRequestExportDto toDto(LeaveRequest entity);

    List<LeaveRequestExportDto> toDtoList(List<LeaveRequest> entities);
}