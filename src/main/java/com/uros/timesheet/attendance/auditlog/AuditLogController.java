package com.uros.timesheet.attendance.auditlog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AuditLogController exposes endpoints for retrieving audit logs,
 * including filtering by user, event type, and paginated queries.
 *
 * All endpoints require appropriate permissions and JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Endpoints for viewing and filtering system audit logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Retrieves all audit logs in the system.
     *
     * @return List of all audit log records
     */
    @Operation(
            summary = "Get all audit logs",
            description = "Retrieves all audit log entries. Requires AUDIT_LOG_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getAll() {
        return ResponseEntity.ok(auditLogService.getAll());
    }

    /**
     * Retrieves audit logs for a specific user.
     *
     * @param userId UUID of the user
     * @return List of audit logs for the specified user
     */
    @Operation(
            summary = "Get audit logs by user",
            description = "Retrieves audit log entries for a specific user. Requires AUDIT_LOG_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs for user retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getByUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        return ResponseEntity.ok(auditLogService.getLogsForUser(userId));
    }

    /**
     * Retrieves audit logs by event type.
     *
     * @param eventType Type of event (e.g., "LOGIN", "CREATE", "DELETE")
     * @return List of audit logs for the specified event type
     */
    @Operation(
            summary = "Get audit logs by event type",
            description = "Retrieves audit log entries by event type. Requires AUDIT_LOG_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs for event type retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/event/{eventType}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getByEvent(
            @Parameter(description = "Event type (e.g., LOGIN, CREATE, DELETE)", required = true)
            @PathVariable String eventType) {
        return ResponseEntity.ok(auditLogService.getLogsByEventType(eventType));
    }

    /**
     * Retrieves a paginated list of audit logs, optionally filtered by event type or user.
     *
     * @param page      Page number (zero-based)
     * @param size      Page size
     * @param eventType Optional event type filter
     * @param userId    Optional user UUID filter
     * @return Paginated audit log results
     */
    @Operation(
            summary = "Get paginated audit logs",
            description = "Retrieves a page of audit logs with optional filtering by event type and/or user. Requires AUDIT_LOG_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuditLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('AUDIT_LOG_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<AuditLogResponse>> getPage(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Optional event type filter", required = false)
            @RequestParam(required = false) String eventType,
            @Parameter(description = "Optional user UUID filter", required = false)
            @RequestParam(required = false) UUID userId) {

        org.springframework.data.domain.Page<AuditLogResponse> result = auditLogService.getLogs(page, size, eventType, userId);
        return ResponseEntity.ok(result);
    }
}