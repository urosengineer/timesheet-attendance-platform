package com.uros.timesheet.attendance.dto.notification;

import com.uros.timesheet.attendance.enums.NotificationType;
import lombok.Data;
import java.util.UUID;

@Data
public class NotificationCreateRequest {
    private UUID recipientId;
    private NotificationType type;
    private String title;
    private String message;
}