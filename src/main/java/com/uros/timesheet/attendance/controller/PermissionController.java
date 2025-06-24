package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.permission.PermissionCreateRequest;
import com.uros.timesheet.attendance.dto.permission.PermissionResponse;
import com.uros.timesheet.attendance.dto.permission.PermissionUpdateRequest;
import com.uros.timesheet.attendance.service.PermissionService;
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
 * PermissionController manages operations related to system permissions,
 * including creation, retrieval, listing, update, and deletion.
 *
 * All endpoints are secured using JWT authentication and require proper authorization.
 */
@RestController
@RequestMapping("/api/v1/permissions")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Endpoints for managing system permissions")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Creates a new permission in the system.
     *
     * @param request Permission creation payload
     * @return The created permission response
     */
    @Operation(
            summary = "Create a new permission",
            description = "Creates a new permission. Requires PERMISSION_CREATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission created successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE') or hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse created = permissionService.createPermission(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves a permission by its unique identifier.
     *
     * @param id Permission UUID
     * @return The permission response
     */
    @Operation(
            summary = "Get permission by ID",
            description = "Retrieves permission details by permission ID. Requires PERMISSION_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission found",
                    content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> getPermission(
            @Parameter(description = "Permission unique identifier", required = true)
            @PathVariable UUID id) {
        PermissionResponse permission = permissionService.getPermissionById(id);
        return ResponseEntity.ok(permission);
    }

    /**
     * Retrieves all permissions in the system.
     *
     * @return List of all permissions
     */
    @Operation(
            summary = "List all permissions",
            description = "Retrieves all permissions. Requires PERMISSION_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    /**
     * Updates an existing permission.
     *
     * @param id Permission UUID to update
     * @param request Permission update payload
     * @return The updated permission response
     */
    @Operation(
            summary = "Update an existing permission",
            description = "Updates an existing permission by ID. Requires PERMISSION_UPDATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission updated successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE') or hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> updatePermission(
            @Parameter(description = "Permission unique identifier", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody PermissionUpdateRequest request) {
        PermissionResponse updated = permissionService.updatePermission(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a permission by its unique identifier.
     *
     * @param id Permission UUID to delete
     * @return The deleted permission response
     */
    @Operation(
            summary = "Delete a permission",
            description = "Deletes a permission by ID. Requires PERMISSION_DELETE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission deleted successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE') or hasRole('ADMIN')")
    public ResponseEntity<PermissionResponse> deletePermission(
            @Parameter(description = "Permission unique identifier", required = true)
            @PathVariable UUID id) {
        PermissionResponse deleted = permissionService.deletePermission(id);
        return ResponseEntity.ok(deleted);
    }
}