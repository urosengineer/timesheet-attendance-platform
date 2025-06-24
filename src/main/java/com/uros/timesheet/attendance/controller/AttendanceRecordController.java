package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordCreateRequest;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.service.AttendanceRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AttendanceRecordController exposes all endpoints for managing attendance records,
 * including workflow actions, soft deletion, restoration, and user/tenant-based filtering.
 *
 * All endpoints are secured using JWT and require proper authorization.
 */
@RestController
@RequestMapping("/api/v1/attendance")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Endpoints for managing employee attendance records")
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;
    private final MessageUtil messages;

    /**
     * Creates a new attendance record.
     *
     * @param request Attendance record creation payload
     * @return The created attendance record response
     */
    @Operation(
            summary = "Create attendance record",
            description = "Creates a new attendance record. Requires ATTENDANCE_CREATE authority or EMPLOYEE/MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record created successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_CREATE') or hasRole('EMPLOYEE') or hasRole('MANAGER')")
    public ResponseEntity<AttendanceRecordResponse> create(
            @Valid @RequestBody AttendanceRecordCreateRequest request) {
        AttendanceRecordResponse created = attendanceRecordService.createRecord(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Submits an attendance record for approval.
     * <p>
     * <b>Business rule:</b> Only the owner of the attendance record (the original user) can submit it, regardless of role or privileges.
     * Users with ATTENDANCE_SUBMIT authority or EMPLOYEE role may invoke this action, but will receive a 403 Forbidden error if they attempt
     * to submit a record not belonging to them.
     * <br>
     * Attempting to submit on behalf of another user (including as ADMIN or HR) is not permitted.
     */
    @Operation(
            summary = "Submit attendance record",
            description = """
        Submits an attendance record for approval.

        **Business rule:** Only the owner of the attendance record (the original user) is permitted to submit it, regardless of role or privileges.  
        Users with ATTENDANCE_SUBMIT authority or EMPLOYEE role may use this endpoint, but will receive a 403 Forbidden error if they attempt to submit a record not belonging to them.

        Attempting to submit on behalf of another user (including as ADMIN or HR) is not permitted.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record submitted successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – Only the owner of the attendance record can submit it.")
    })
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('ATTENDANCE_SUBMIT') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecordResponse> submit(
            @Parameter(description = "Attendance record UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the user submitting the record", required = true)
            @RequestParam UUID userId) {
        AttendanceRecordResponse updated = attendanceRecordService.submitRecord(id, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Approves an attendance record.
     *
     * @param id         Attendance record UUID
     * @param approverId UUID of the approver
     * @return The updated attendance record response
     */
    @Operation(
            summary = "Approve attendance record",
            description = "Approves an attendance record. Requires ATTENDANCE_APPROVE authority or MANAGER/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record approved successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ATTENDANCE_APPROVE') or hasRole('MANAGER') or hasRole('HR')")
    public ResponseEntity<AttendanceRecordResponse> approve(
            @Parameter(description = "Attendance record UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the approver", required = true)
            @RequestParam UUID approverId) {
        AttendanceRecordResponse updated = attendanceRecordService.approveRecord(id, approverId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Rejects an attendance record.
     *
     * @param id         Attendance record UUID
     * @param approverId UUID of the approver
     * @param reason     Reason for rejection
     * @return The updated attendance record response
     */
    @Operation(
            summary = "Reject attendance record",
            description = "Rejects an attendance record with a specified reason. Requires ATTENDANCE_REJECT authority or MANAGER/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record rejected successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ATTENDANCE_REJECT') or hasRole('MANAGER') or hasRole('HR')")
    public ResponseEntity<AttendanceRecordResponse> reject(
            @PathVariable UUID id,
            @RequestParam UUID approverId,
            @RequestParam String reason) {
        AttendanceRecordResponse updated = attendanceRecordService.rejectRecord(id, approverId, reason);
        return ResponseEntity.ok(updated);
    }

    /**
     * Retrieves a specific attendance record by its ID.
     *
     * @param id Attendance record UUID
     * @return The attendance record response
     */
    @Operation(
            summary = "Get attendance record by ID",
            description = "Retrieves a single attendance record by its unique ID. Requires ATTENDANCE_VIEW authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record found",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<AttendanceRecordResponse> get(
            @Parameter(description = "Attendance record UUID", required = true)
            @PathVariable UUID id) {
        AttendanceRecordResponse record = attendanceRecordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }

    /**
     * Retrieves all attendance records for a specific user.
     *
     * @param userId User UUID
     * @return List of attendance records for the specified user
     */
    @Operation(
            summary = "List all attendance records for a user",
            description = "Retrieves all attendance records for the specified user. Requires ATTENDANCE_VIEW_SELF authority or HR/ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ATTENDANCE_VIEW_SELF') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceRecordResponse>> listForUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        List<AttendanceRecordResponse> records = attendanceRecordService.getRecordsForUser(userId);
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves all attendance records for the current tenant (organization).
     *
     * @return List of attendance records for the current tenant
     */
    @Operation(
            summary = "List all attendance records for current tenant",
            description = "Retrieves all attendance records for the current tenant (multi-tenancy). Requires ADMIN/MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance records retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/tenant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceRecordResponse>> listForTenant() {
        List<AttendanceRecordResponse> records = attendanceRecordService.getRecordsForCurrentTenant();
        return ResponseEntity.ok(records);
    }

    /**
     * Performs a soft delete on an attendance record by marking it as deleted.
     *
     * @param id          Attendance record UUID to be deleted
     * @param performedBy UUID of the user performing the deletion
     * @param reason      Reason for deletion (optional)
     * @return The deleted attendance record response
     */
    @Operation(
            summary = "Soft delete attendance record",
            description = "Marks an attendance record as deleted. Requires ATTENDANCE_DELETE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record soft-deleted successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecordResponse> softDelete(
            @PathVariable UUID id,
            @RequestParam UUID performedBy,
            @RequestParam(required = false) String reason) {
        String resolvedReason = (reason != null) ? reason : messages.get("attendance.no.reason.provided");
        AttendanceRecordResponse deleted = attendanceRecordService.softDeleteRecord(id, performedBy, resolvedReason);
        return ResponseEntity.ok(deleted);
    }

    /**
     * Restores a previously soft-deleted attendance record.
     *
     * @param id          Attendance record UUID to be restored
     * @param performedBy UUID of the user performing the restoration
     * @param reason      Reason for restoration (optional)
     * @return The restored attendance record response
     */
    @Operation(
            summary = "Restore soft-deleted attendance record",
            description = "Restores a previously deleted attendance record. Requires ATTENDANCE_RESTORE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attendance record restored successfully",
                    content = @Content(schema = @Schema(implementation = AttendanceRecordResponse.class))),
            @ApiResponse(responseCode = "404", description = "Attendance record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ATTENDANCE_RESTORE') or hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecordResponse> restore(
            @PathVariable UUID id,
            @RequestParam UUID performedBy,
            @RequestParam(required = false) String reason) {
        String resolvedReason = (reason != null) ? reason : messages.get("attendance.no.reason.provided");
        AttendanceRecordResponse restored = attendanceRecordService.restoreRecord(id, performedBy, resolvedReason);
        return ResponseEntity.ok(restored);
    }
}