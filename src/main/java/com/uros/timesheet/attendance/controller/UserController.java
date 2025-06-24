package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.service.UserService;
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
 * UserController handles user management operations such as creation, retrieval,
 * soft deletion, and restoration.
 *
 * All endpoints are secured using JWT and require proper authorization.
 */
@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing users in the system")
public class UserController {

    private final UserService userService;
    private final MessageUtil messageUtil;

    /**
     * Creates a new user in the system.
     *
     * @param request User creation request payload
     * @return The created user response
     */
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user. Requires USER_CREATE authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id User UUID
     * @return The user response
     */
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves user details by user ID. Requires USER_VIEW authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "User unique identifier", required = true)
            @PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves all users for the current tenant (organization).
     *
     * @return List of users for the current tenant
     */
    @Operation(
            summary = "List users for current tenant",
            description = "Retrieves all users associated with the current tenant. Requires ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/current-tenant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<List<UserResponse>> getUsersForCurrentTenant() {
        List<UserResponse> users = userService.getUsersForCurrentTenant();
        return ResponseEntity.ok(users);
    }

    /**
     * Performs a soft delete on a user by marking them as deleted.
     *
     * @param id          User UUID to be deleted
     * @param performedBy UUID of the user performing the action
     * @param reason      Reason for deletion (optional)
     * @return The deleted user response
     */
    @Operation(
            summary = "Soft delete a user",
            description = "Marks a user as deleted. Requires USER_DELETE authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User soft-deleted successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<UserResponse> softDeleteUser(
            @Parameter(description = "User unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for deletion", required = false)
            @RequestParam(required = false) String reason) {
        UserResponse deleted = userService.softDeleteUser(
                id, performedBy, reason != null ? reason : messageUtil.get("user.no.reason.provided")
        );
        return ResponseEntity.ok(deleted);
    }

    /**
     * Restores a previously soft-deleted user.
     *
     * @param id          User UUID to be restored
     * @param performedBy UUID of the user performing the action
     * @param reason      Reason for restoration (optional)
     * @return The restored user response
     */
    @Operation(
            summary = "Restore a soft-deleted user",
            description = "Restores a user who was previously marked as deleted. Requires USER_RESTORE authority or ADMIN/HR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User restored successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('USER_RESTORE') or hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<UserResponse> restoreUser(
            @Parameter(description = "User unique identifier", required = true)
            @PathVariable UUID id,
            @Parameter(description = "ID of the user performing the action", required = true)
            @RequestParam UUID performedBy,
            @Parameter(description = "Reason for restoration", required = false)
            @RequestParam(required = false) String reason) {
        UserResponse restored = userService.restoreUser(
                id, performedBy, reason != null ? reason : messageUtil.get("user.no.reason.provided")
        );
        return ResponseEntity.ok(restored);
    }
}