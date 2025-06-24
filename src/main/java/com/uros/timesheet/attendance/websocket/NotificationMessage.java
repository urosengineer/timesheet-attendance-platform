package com.uros.timesheet.attendance.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for notification messages delivered via WebSocket.
 * Used for real-time notification updates in the attendance application.
 */
@Data
@AllArgsConstructor
public class NotificationMessage {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private String status;
    private Instant createdAt;
}