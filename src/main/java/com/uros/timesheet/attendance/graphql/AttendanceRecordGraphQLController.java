package com.uros.timesheet.attendance.graphql;

import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AttendanceRecordGraphQLController {

    private final AttendanceRecordService attendanceRecordService;

    @QueryMapping
    public AttendanceRecordResponse attendanceRecord(@Argument UUID id) {
        return attendanceRecordService.getRecordById(id);
    }

    @QueryMapping
    public List<AttendanceRecordResponse> attendanceRecordsForUser(@Argument UUID userId) {
        return attendanceRecordService.getRecordsForUser(userId);
    }
}