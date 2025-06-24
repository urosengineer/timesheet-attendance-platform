package com.uros.timesheet.attendance.service.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import org.springframework.core.io.Resource;
import java.util.List;

public interface AttendanceSummaryExcelExportService {
    Resource exportSummaryToExcel(List<UserAttendanceSummaryDto> summaries);
}