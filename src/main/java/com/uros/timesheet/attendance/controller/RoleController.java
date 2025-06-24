package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.role.RoleCreateRequest;
import com.uros.timesheet.attendance.dto.role.RoleResponse;
import com.uros.timesheet.attendance.dto.role.RoleUpdateRequest;
import com.uros.timesheet.attendance.service.RoleService;
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
 * RoleController handles role management operations such as creation, retrieval,
 * update, soft deletion, and restoration.
 *
 * All endpoints are secured using JWT and require proper authorization.
 */
@RestController
@RequestMapping("/api/v1/roles")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Endpoints for managing roles in the system")
public class RoleController {

    private final RoleService roleService;

    /**
     * Creates a new role in the system.
     */
    @Operation(
            summary = "Create a new role",
            description = "Creates a new role. Requires ROLE_CREATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody RoleCreateRequest request) {
        RoleResponse created = roleService.createRole(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves a role by its unique identifier.
     */
    @Operation(
            summary = "Get role by ID",
            description = "Retrieves role details by role ID. Requires ROLE_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role found",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> getRole(
            @Parameter(description = "Role unique identifier", required = true)
            @PathVariable UUID id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Retrieves all roles in the system.
     */
    @Operation(
            summary = "List all roles",
            description = "Retrieves all roles in the system. Requires ROLE_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Updates an existing role.
     */
    @Operation(
            summary = "Update an existing role",
            description = "Updates role details. Requires ROLE_UPDATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> updateRole(
            @Parameter(description = "Role unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = true)
            @RequestParam UUID performedBy,
            @Valid @RequestBody RoleUpdateRequest request) {
        RoleResponse updated = roleService.updateRole(id, request, performedBy);
        return ResponseEntity.ok(updated);
    }

    /**
     * Performs a soft delete on a role by marking it as deleted.
     */
    @Operation(
            summary = "Soft delete a role",
            description = "Marks a role as deleted. Requires ROLE_DELETE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role soft-deleted successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> softDeleteRole(
            @Parameter(description = "Role unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for deletion", required = false)
            @RequestParam(required = false) String reason) {
        RoleResponse deleted = roleService.softDeleteRole(id, performedBy, reason != null ? reason : "No reason provided");
        return ResponseEntity.ok(deleted);
    }

    /**
     * Restores a previously soft-deleted role.
     */
    @Operation(
            summary = "Restore a soft-deleted role",
            description = "Restores a role that was previously marked as deleted. Requires ROLE_RESTORE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role restored successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ROLE_RESTORE') or hasRole('ADMIN')")
    public ResponseEntity<RoleResponse> restoreRole(
            @Parameter(description = "Role unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for restoration", required = false)
            @RequestParam(required = false) String reason) {
        RoleResponse restored = roleService.restoreRole(id, performedBy, reason != null ? reason : "No reason provided");
        return ResponseEntity.ok(restored);
    }
}