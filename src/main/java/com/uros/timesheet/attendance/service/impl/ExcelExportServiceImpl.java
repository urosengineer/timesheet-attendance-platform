package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.dto.export.AttendanceRecordExportDto;
import com.uros.timesheet.attendance.dto.export.ExportRequest;
import com.uros.timesheet.attendance.dto.export.LeaveRequestExportDto;
import com.uros.timesheet.attendance.exception.ExportException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.AttendanceRecordExportMapper;
import com.uros.timesheet.attendance.mapper.LeaveRequestExportMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("excelExportService")
@RequiredArgsConstructor
@Slf4j
public class ExcelExportServiceImpl implements ExportService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final MessageUtil messageUtil;
    private final AttendanceRecordExportMapper attendanceMapper;
    private final LeaveRequestExportMapper leaveMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public Resource exportToExcel(ExportRequest request) throws ExportException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            switch (request.getExportType()) {
                case ATTENDANCE:
                    return exportAttendanceToExcel(workbook, request, headerStyle, dataStyle);
                case LEAVE_REQUESTS:
                    return exportLeaveRequestsToExcel(workbook, request, headerStyle, dataStyle);
                case ALL:
                    return exportCombinedToExcel(workbook, request, headerStyle, dataStyle);
                default:
                    throw new ExportException(messageUtil.get("export.error.invalid.type"));
            }
        } catch (IOException e) {
            log.error("Excel export failed", e);
            throw new ExportException(messageUtil.get("export.error.excel.generation"));
        }
    }

    private Resource exportAttendanceToExcel(Workbook workbook, ExportRequest request,
                                             CellStyle headerStyle, CellStyle dataStyle) throws IOException {
        Sheet sheet = workbook.createSheet(messageUtil.get("export.sheet.attendance"));

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                messageUtil.get("export.column.date"),
                messageUtil.get("export.column.start"),
                messageUtil.get("export.column.end"),
                messageUtil.get("export.column.type"),
                messageUtil.get("export.column.status"),
                messageUtil.get("export.column.notes")
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<AttendanceRecord> records = attendanceRecordRepository.findByUserIdAndDateBetween(
                request.getUserId(), request.getStartDate(), request.getEndDate());
        List<AttendanceRecordExportDto> dtoList = attendanceMapper.toDtoList(records);

        int rowNum = 1;
        for (AttendanceRecordExportDto dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getDate());
            row.createCell(1).setCellValue(dto.getStartTime());
            row.createCell(2).setCellValue(dto.getEndTime());
            row.createCell(3).setCellValue(dto.getType());
            row.createCell(4).setCellValue(dto.getStatus());
            row.createCell(5).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return createResource(workbook);
    }

    private Resource exportLeaveRequestsToExcel(Workbook workbook, ExportRequest request,
                                                CellStyle headerStyle, CellStyle dataStyle) throws IOException {
        Sheet sheet = workbook.createSheet(messageUtil.get("export.sheet.leave"));

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                messageUtil.get("export.column.start.date"),
                messageUtil.get("export.column.end.date"),
                messageUtil.get("export.column.type"),
                messageUtil.get("export.column.status"),
                messageUtil.get("export.column.approved.by"),
                messageUtil.get("export.column.notes")
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<LeaveRequest> leaves = leaveRequestRepository.findByUserIdAndStartDateBetween(
                request.getUserId(), request.getStartDate(), request.getEndDate());
        List<LeaveRequestExportDto> dtoList = leaveMapper.toDtoList(leaves);

        int rowNum = 1;
        for (LeaveRequestExportDto dto : dtoList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getStartDate());
            row.createCell(1).setCellValue(dto.getEndDate());
            row.createCell(2).setCellValue(dto.getType());
            row.createCell(3).setCellValue(dto.getStatus());
            row.createCell(4).setCellValue(dto.getApprover() != null ? dto.getApprover() : "");
            row.createCell(5).setCellValue(dto.getNotes() != null ? dto.getNotes() : "");
            for (int i = 0; i < headers.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return createResource(workbook);
    }

    private Resource exportCombinedToExcel(Workbook workbook, ExportRequest request,
                                           CellStyle headerStyle, CellStyle dataStyle) throws IOException {
        exportAttendanceToExcel(workbook, request, headerStyle, dataStyle);
        exportLeaveRequestsToExcel(workbook, request, headerStyle, dataStyle);
        return createResource(workbook);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private Resource createResource(Workbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayResource(out.toByteArray());
    }

    @Override
    public Resource exportToPdf(ExportRequest request) throws ExportException {
        throw new UnsupportedOperationException(messageUtil.get("export.error.pdf.not.implemented"));
    }
}