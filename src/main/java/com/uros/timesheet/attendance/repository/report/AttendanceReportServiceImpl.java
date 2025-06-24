package com.uros.timesheet.attendance.service.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import com.uros.timesheet.attendance.repository.report.AttendanceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final AttendanceReportRepository attendanceReportRepository;

    @Override
    public List<UserAttendanceSummaryDto> getUserAttendanceSummary(UUID userId, LocalDate from, LocalDate to) {
        return attendanceReportRepository.getUserAttendanceSummary(userId, from, to);
    }
}