package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.dto.export.AttendanceRecordExportDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttendanceRecordExportMapper {
    @Mapping(target = "date", expression = "java(entity.getDate().format(java.time.format.DateTimeFormatter.ofPattern(\"dd.MM.yyyy\")))")
    @Mapping(target = "startTime", expression = "java(entity.getStartTime() != null ? entity.getStartTime().toString() : \"\")")
    @Mapping(target = "endTime", expression = "java(entity.getEndTime() != null ? entity.getEndTime().toString() : \"\")")
    AttendanceRecordExportDto toDto(AttendanceRecord entity);

    List<AttendanceRecordExportDto> toDtoList(List<AttendanceRecord> entities);
}