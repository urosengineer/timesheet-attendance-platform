package com.uros.timesheet.attendance.service.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceReportService {
    List<UserAttendanceSummaryDto> getUserAttendanceSummary(UUID userId, LocalDate from, LocalDate to);
}