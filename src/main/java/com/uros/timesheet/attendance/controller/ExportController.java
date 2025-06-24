package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.export.ExportRequest;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.ExportService;
import com.uros.timesheet.attendance.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ExportController exposes endpoints for exporting attendance and leave data
 * in PDF and Excel formats. All endpoints are secured and support
 * user and date range filters.
 */
@RestController
@RequestMapping("/api/v1/export")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Export", description = "Endpoints for exporting attendance and leave data in PDF/Excel format")
public class ExportController {

    private final ExportService pdfExportService;
    private final ExportService excelExportService;
    private final UserRepository userRepository;

    public ExportController(
            @Qualifier("pdfExportService") ExportService pdfExportService,
            @Qualifier("excelExportService") ExportService excelExportService,
            UserRepository userRepository
    ) {
        this.pdfExportService = pdfExportService;
        this.excelExportService = excelExportService;
        this.userRepository = userRepository;
    }

    /**
     * Exports attendance or leave data to PDF.
     *
     * @param userId      UUID of the user whose data is being exported
     * @param startDate   Start date of the export range
     * @param endDate     End date of the export range
     * @param exportType  Type of export (e.g. ATTENDANCE, LEAVE)
     * @param userDetails Authenticated user details
     * @return PDF file as a downloadable resource
     */
    @Operation(
            summary = "Export data to PDF",
            description = "Exports attendance or leave data to PDF format. Requires EXPORT_PDF authority or HR/MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF exported successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/pdf")
    @PreAuthorize("hasAuthority('EXPORT_PDF') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<Resource> exportToPdf(
            @Parameter(description = "UUID of the user whose data is exported", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Start date for export", required = true, example = "2024-06-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date for export", required = true, example = "2024-06-30")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Type of export", required = true, schema = @Schema(implementation = ExportRequest.ExportType.class))
            @RequestParam ExportRequest.ExportType exportType,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {

        User requestedBy = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));

        ExportRequest request = new ExportRequest();
        request.setUserId(userId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setExportType(exportType);
        request.setRequestedBy(requestedBy);

        Resource resource = pdfExportService.exportToPdf(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + generateFileName(request, "pdf") + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    /**
     * Exports attendance or leave data to Excel.
     *
     * @param userId      UUID of the user whose data is being exported
     * @param startDate   Start date of the export range
     * @param endDate     End date of the export range
     * @param exportType  Type of export (e.g. ATTENDANCE, LEAVE)
     * @param userDetails Authenticated user details
     * @return Excel file as a downloadable resource
     */
    @Operation(
            summary = "Export data to Excel",
            description = "Exports attendance or leave data to Excel format. Requires EXPORT_EXCEL authority or HR/MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel exported successfully",
                    content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/excel")
    @PreAuthorize("hasAuthority('EXPORT_EXCEL') or hasRole('HR') or hasRole('MANAGER')")
    public ResponseEntity<Resource> exportToExcel(
            @Parameter(description = "UUID of the user whose data is exported", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Start date for export", required = true, example = "2024-06-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "End date for export", required = true, example = "2024-06-30")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Type of export", required = true, schema = @Schema(implementation = ExportRequest.ExportType.class))
            @RequestParam ExportRequest.ExportType exportType,
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {

        User requestedBy = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));

        ExportRequest request = new ExportRequest();
        request.setUserId(userId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setExportType(exportType);
        request.setRequestedBy(requestedBy);

        Resource resource = excelExportService.exportToExcel(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + generateFileName(request, "xlsx") + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
    }

    /**
     * Generates the export file name based on export parameters and extension.
     *
     * @param request   Export request
     * @param extension File extension
     * @return Generated file name
     */
    private String generateFileName(ExportRequest request, String extension) {
        return String.format("%s_%s_%s_%s.%s",
                request.getExportType().name().toLowerCase(),
                request.getUserId(),
                request.getStartDate(),
                request.getEndDate(),
                extension);
    }
}