package com.uros.timesheet.attendance.controller.report;

import com.uros.timesheet.attendance.dto.report.UserAttendanceSummaryDto;
import com.uros.timesheet.attendance.service.report.AttendanceReportService;
import com.uros.timesheet.attendance.service.report.AttendanceSummaryExportService;
import com.uros.timesheet.attendance.service.report.AttendanceSummaryExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * AttendanceReportController exposes endpoints for retrieving and exporting
 * user attendance summaries in CSV and Excel formats.
 * <p>
 * All endpoints are secured and support filtering by user and date range.
 */
@RestController
@RequestMapping("/api/v1/reports/attendance")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Attendance Reports", description = "Endpoints for user attendance reporting and export")
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;
    private final AttendanceSummaryExportService attendanceSummaryExportService;
    private final AttendanceSummaryExcelExportService attendanceSummaryExcelExportService;

    /**
     * Retrieves attendance summary for a specific user within a date range.
     *
     * @param userId UUID of the user
     * @param from   Start date (inclusive)
     * @param to     End date (inclusive)
     * @return List of attendance summary DTOs
     */
    @Operation(
            summary = "Get user attendance summary",
            description = "Retrieves the attendance summary for a user in the specified date range. " +
                    "Requires REPORT_VIEW authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserAttendanceSummaryDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('REPORT_VIEW') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<UserAttendanceSummaryDto>> getUserAttendanceSummary(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Start date (inclusive)", required = true, example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)", required = true, example = "2024-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<UserAttendanceSummaryDto> summary = attendanceReportService.getUserAttendanceSummary(userId, from, to);
        return ResponseEntity.ok(summary);
    }

    /**
     * Exports user attendance summary as a CSV file.
     *
     * @param userId UUID of the user
     * @param from   Start date (inclusive)
     * @param to     End date (inclusive)
     * @return CSV file as a downloadable resource
     */
    @Operation(
            summary = "Export user attendance summary (CSV)",
            description = "Exports the user's attendance summary to CSV format. " +
                    "Requires REPORT_EXPORT authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CSV file exported successfully",
                    content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}/csv")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Resource> exportUserAttendanceSummaryCsv(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Start date (inclusive)", required = true, example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)", required = true, example = "2024-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<UserAttendanceSummaryDto> summary = attendanceReportService.getUserAttendanceSummary(userId, from, to);
        Resource resource = attendanceSummaryExportService.exportSummaryToCsv(summary);

        String filename = String.format("attendance_summary_%s_%s_%s.csv", userId, from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    /**
     * Exports user attendance summary as an Excel file.
     *
     * @param userId UUID of the user
     * @param from   Start date (inclusive)
     * @param to     End date (inclusive)
     * @return Excel file as a downloadable resource
     */
    @Operation(
            summary = "Export user attendance summary (Excel)",
            description = "Exports the user's attendance summary to Excel format. " +
                    "Requires REPORT_EXPORT authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel file exported successfully",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}/excel")
    @PreAuthorize("hasAuthority('REPORT_EXPORT') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<Resource> exportUserAttendanceSummaryExcel(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Start date (inclusive)", required = true, example = "2024-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)", required = true, example = "2024-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        List<UserAttendanceSummaryDto> summary = attendanceReportService.getUserAttendanceSummary(userId, from, to);
        Resource resource = attendanceSummaryExcelExportService.exportSummaryToExcel(summary);

        String filename = String.format("attendance_summary_%s_%s_%s.xlsx", userId, from, to);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }
}