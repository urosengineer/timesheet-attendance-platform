package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.leave.LeaveRequestCreateRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.service.LeaveRequestService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * LeaveRequestController manages operations related to leave requests,
 * including creation, submission, approval, rejection, retrieval, soft deletion, and restoration.
 *
 * All endpoints are secured using JWT authentication and require appropriate authorization.
 */
@RestController
@RequestMapping("/api/v1/leave-requests")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Endpoints for managing employee leave requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final MessageUtil messages;

    /**
     * Creates a new leave request in DRAFT status.
     *
     * @param request Leave request creation payload
     * @return The created leave request response
     */
    @Operation(
            summary = "Create leave request",
            description = "Creates a new leave request in DRAFT status. Requires LEAVE_REQUEST_CREATE authority or EMPLOYEE role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request created successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_CREATE') or hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveRequestResponse> createLeaveRequest(
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        LeaveRequestResponse created = leaveRequestService.createRequest(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Submits a leave request for approval.
     * Only the owner of the leave request can submit it.
     *
     * @param id Leave request UUID
     * @param principal Authenticated user principal
     * @return The submitted leave request response
     */
    @Operation(
            summary = "Submit leave request",
            description = """
                Submits a leave request for approval.

                **Business rule:** Only the owner of the leave request can submit it. 
                Users with LEAVE_REQUEST_SUBMIT authority or EMPLOYEE role may use this endpoint, but will receive a 403 Forbidden error if they attempt to submit a request not belonging to them.

                Attempting to submit on behalf of another user (including as ADMIN or HR) is not permitted.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request submitted successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden – Only the owner of the leave request can submit it.")
    })
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_SUBMIT') or hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> submitLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LeaveRequestResponse updated = leaveRequestService.submitRequest(id, principal);
        return ResponseEntity.ok(updated);
    }

    /**
     * Approves a leave request.
     *
     * @param id Leave request UUID
     * @param approverId UUID of the approver
     * @return The approved leave request response
     */
    @Operation(
            summary = "Approve leave request",
            description = "Approves a leave request. Requires LEAVE_REQUEST_APPROVE authority or MANAGER/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request approved successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_APPROVE') or hasRole('MANAGER') or hasRole('HR')")
    public ResponseEntity<LeaveRequestResponse> approveLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the approver", required = true)
            @RequestParam UUID approverId) {
        LeaveRequestResponse updated = leaveRequestService.approveRequest(id, approverId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Rejects a leave request with a specified reason.
     *
     * @param id Leave request UUID
     * @param approverId UUID of the approver
     * @param reason Reason for rejection
     * @return The rejected leave request response
     */
    @Operation(
            summary = "Reject leave request",
            description = "Rejects a leave request with a reason. Requires LEAVE_REQUEST_REJECT authority or MANAGER/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request rejected successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_REJECT') or hasRole('MANAGER') or hasRole('HR')")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the approver", required = true)
            @RequestParam UUID approverId,
            @Parameter(description = "Reason for rejection", required = true)
            @RequestParam String reason) {
        LeaveRequestResponse updated = leaveRequestService.rejectRequest(id, approverId, reason);
        return ResponseEntity.ok(updated);
    }

    /**
     * Retrieves a leave request by its unique identifier.
     *
     * @param id Leave request UUID
     * @return The leave request response
     */
    @Operation(
            summary = "Get leave request by ID",
            description = "Retrieves a leave request by ID. Requires LEAVE_REQUEST_VIEW authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request found",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Leave request not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_VIEW') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<LeaveRequestResponse> getLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id) {
        LeaveRequestResponse record = leaveRequestService.getRequestById(id);
        return ResponseEntity.ok(record);
    }

    /**
     * Retrieves all leave requests for a specific user.
     *
     * @param userId User UUID
     * @return List of leave requests for the specified user
     */
    @Operation(
            summary = "List leave requests for user",
            description = "Retrieves all leave requests for the specified user. Requires LEAVE_REQUEST_VIEW_SELF authority or HR/ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_VIEW_SELF') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestResponse>> listLeaveRequestsForUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId) {
        List<LeaveRequestResponse> records = leaveRequestService.getRequestsForUser(userId);
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves all leave requests for the current tenant (organization).
     *
     * @return List of leave requests for the current tenant
     */
    @Operation(
            summary = "List leave requests for current tenant",
            description = "Retrieves all leave requests for the current tenant. Requires ADMIN or MANAGER role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave requests retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/current-tenant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<LeaveRequestResponse>> listLeaveRequestsForCurrentTenant() {
        List<LeaveRequestResponse> leaveRequests = leaveRequestService.getRequestsForCurrentTenant();
        return ResponseEntity.ok(leaveRequests);
    }

    /**
     * Marks a leave request as deleted (soft delete).
     *
     * @param id Leave request UUID
     * @param performedBy UUID of the user performing the deletion
     * @param reason Reason for deletion (optional)
     * @return The deleted leave request response
     */
    @Operation(
            summary = "Soft delete leave request",
            description = "Marks the leave request as deleted (soft delete). Requires LEAVE_REQUEST_DELETE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request marked as deleted",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> softDeleteLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the user performing the deletion", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for deletion", required = false)
            @RequestParam(required = false) String reason) {
        String resolvedReason = (reason != null) ? reason : messages.get("leaverequest.no.reason.provided");
        LeaveRequestResponse deleted = leaveRequestService.softDeleteRequest(id, performedBy, resolvedReason);
        return ResponseEntity.ok(deleted);
    }

    /**
     * Restores a previously soft-deleted leave request.
     *
     * @param id Leave request UUID
     * @param performedBy UUID of the user performing the restoration
     * @param reason Reason for restoration (optional)
     * @return The restored leave request response
     */
    @Operation(
            summary = "Restore deleted leave request",
            description = "Restores a soft-deleted leave request. Requires LEAVE_REQUEST_RESTORE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leave request restored",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_RESTORE') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestResponse> restoreLeaveRequest(
            @Parameter(description = "Leave request UUID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "UUID of the user performing the restoration", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for restoration", required = false)
            @RequestParam(required = false) String reason) {
        String resolvedReason = (reason != null) ? reason : messages.get("leaverequest.no.reason.provided");
        LeaveRequestResponse restored = leaveRequestService.restoreRequest(id, performedBy, resolvedReason);
        return ResponseEntity.ok(restored);
    }
}