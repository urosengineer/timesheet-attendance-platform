package com.uros.timesheet.attendance.controller;

import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import com.uros.timesheet.attendance.service.NotificationService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing user notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications and delivery")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Creates and immediately sends a notification to a user using the appropriate delivery channel.
     *
     * @param request Notification creation payload
     * @return The created and sent notification response
     */
    @Operation(
            summary = "Create and send notification",
            description = "Creates and immediately sends a notification to the specified recipient using the channel matching the notification type. Requires NOTIFICATION_CREATE authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification created and sent successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_CREATE') or hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createAndSend(
            @Valid @RequestBody NotificationCreateRequest request) {
        NotificationResponse created = notificationService.createAndSend(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Retrieves a notification by its unique identifier.
     *
     * @param id Notification UUID
     * @return The notification response
     */
    @Operation(
            summary = "Get notification by ID",
            description = "Retrieves notification details by notification ID. Requires NOTIFICATION_VIEW authority or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW') or hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> getNotification(
            @Parameter(description = "Notification unique identifier", required = true)
            @PathVariable UUID id) {
        NotificationResponse response = notificationService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves notifications sent to a recipient, paginated.
     *
     * @param recipientId Recipient user UUID
     * @param pageable    Pagination information (page, size, sort)
     * @return Paginated notifications for the specified recipient
     */
    @Operation(
            summary = "Get paginated notifications for recipient",
            description = "Retrieves paginated notifications addressed to the specified recipient. Requires NOTIFICATION_VIEW_SELF authority (for self) or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/recipient/{recipientId}/page")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW_SELF') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationResponse>> getForRecipientPaginated(
            @Parameter(description = "Recipient user unique identifier", required = true)
            @PathVariable UUID recipientId,
            @ParameterObject Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getForRecipientPaginated(recipientId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Retrieves all notifications sent to a given recipient user (non-paginated, legacy).
     *
     * @param recipientId Recipient user UUID
     * @return List of notifications for the specified recipient
     */
    @Operation(
            summary = "Get all notifications for recipient (legacy, non-paginated)",
            description = "Retrieves all notifications addressed to the specified recipient. Requires NOTIFICATION_VIEW_SELF authority (for self) or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/recipient/{recipientId}")
    @PreAuthorize("hasAuthority('NOTIFICATION_VIEW_SELF') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getForRecipient(
            @Parameter(description = "Recipient user unique identifier", required = true)
            @PathVariable UUID recipientId) {
        List<NotificationResponse> notifications = notificationService.getForRecipient(recipientId);
        return ResponseEntity.ok(notifications);
    }
}