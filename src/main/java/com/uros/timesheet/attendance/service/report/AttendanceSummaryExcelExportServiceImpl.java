package com.uros.timesheet.attendance.service.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@Slf4j
public class AttendanceSummaryExcelExportServiceImpl implements AttendanceSummaryExcelExportService {

    @Override
    public Resource exportSummaryToExcel(List<UserAttendanceSummaryDto> summaries) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Summary");

            // Header styling
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "User ID", "User Full Name", "From", "To",
                    "Total Days", "Total Records", "Total Hours"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data styling
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);

            // Data rows
            int rowNum = 1;
            for (UserAttendanceSummaryDto dto : summaries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dto.getUserId().toString());
                row.createCell(1).setCellValue(dto.getUserFullName());
                row.createCell(2).setCellValue(dto.getFromDate().toString());
                row.createCell(3).setCellValue(dto.getToDate().toString());
                row.createCell(4).setCellValue(dto.getTotalDays());
                row.createCell(5).setCellValue(dto.getTotalRecords());
                row.createCell(6).setCellValue(dto.getTotalHours() != null ? dto.getTotalHours().doubleValue() : 0.0);

                // Apply data style to each cell
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());

        } catch (Exception e) {
            log.error("Excel export failed", e);
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        }
    }
}