package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface AttendanceRecordMapper {
    AttendanceRecordResponse toResponse(AttendanceRecord entity);
}