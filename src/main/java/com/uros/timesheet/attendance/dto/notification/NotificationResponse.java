package com.uros.timesheet.attendance.dto.notification;

import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private UserResponse recipient;
    private NotificationType type;
    private String title;
    private String message;
    private String status;
    private Instant sentAt;
    private Instant createdAt;
}