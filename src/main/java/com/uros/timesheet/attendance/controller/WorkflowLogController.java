package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.workflow.WorkflowLogResponse;
import com.uros.timesheet.attendance.service.WorkflowLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * WorkflowLogController exposes endpoints for retrieving workflow log history
 * for any entity supporting workflow transitions (e.g., attendance records, requests).
 * <p>
 * All endpoints require JWT authentication and proper authority.
 */
@RestController
@RequestMapping("/api/v1/workflow-logs")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Workflow Logs", description = "Endpoints for retrieving workflow transition logs for entities")
public class WorkflowLogController {

    private final WorkflowLogService workflowLogService;

    /**
     * Retrieves workflow log entries for a given entity type and ID.
     *
     * @param entityType Type of the entity (e.g., "ATTENDANCE_RECORD", "LEAVE_REQUEST")
     * @param entityId   UUID of the entity
     * @return List of workflow log responses for the specified entity
     */
    @Operation(
            summary = "Get workflow logs for entity",
            description = "Retrieves all workflow log entries for a given entity type and ID. " +
                    "Requires WORKFLOW_LOG_VIEW authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workflow logs retrieved successfully",
                    content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = WorkflowLogResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Entity or workflow logs not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('WORKFLOW_LOG_VIEW') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<WorkflowLogResponse>> getLogs(
            @Parameter(description = "Type of the entity (e.g., ATTENDANCE_RECORD, LEAVE_REQUEST)", required = true)
            @PathVariable String entityType,
            @Parameter(description = "UUID of the entity", required = true)
            @PathVariable UUID entityId) {

        List<WorkflowLogResponse> logs = workflowLogService.getLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(logs);
    }
}