package com.uros.timesheet.attendance.service.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class AttendanceSummaryExportServiceImpl implements AttendanceSummaryExportService {

    @Override
    public Resource exportSummaryToCsv(List<UserAttendanceSummaryDto> summaries) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // Header
            writer.println("User ID,User Full Name,From,To,Total Days,Total Records,Total Hours");

            // Rows
            for (UserAttendanceSummaryDto dto : summaries) {
                writer.printf("%s,%s,%s,%s,%d,%d,%.2f%n",
                        escapeCsv(dto.getUserId().toString()),
                        escapeCsv(dto.getUserFullName()),
                        escapeCsv(dto.getFromDate().toString()),
                        escapeCsv(dto.getToDate().toString()),
                        dto.getTotalDays(),
                        dto.getTotalRecords(),
                        dto.getTotalHours() != null ? dto.getTotalHours() : 0.0
                );
            }

            writer.flush();
            return new ByteArrayResource(out.toByteArray());

        } catch (Exception e) {
            log.error("CSV export failed", e);
            throw new RuntimeException("CSV export failed: " + e.getMessage(), e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean needEscape = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (needEscape) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}