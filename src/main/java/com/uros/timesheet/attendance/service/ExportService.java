package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.export.ExportRequest;
import com.uros.timesheet.attendance.exception.ExportException;
import org.springframework.core.io.Resource;

public interface ExportService {
    Resource exportToPdf(ExportRequest request) throws ExportException;
    Resource exportToExcel(ExportRequest request) throws ExportException;
}