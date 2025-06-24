package com.uros.timesheet.attendance.notification;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketNotificationChannel implements NotificationChannel {

    @Override
    public NotificationType getType() {
        return NotificationType.WEBSOCKET;
    }

    @Override
    public boolean send(Notification notification) {
        log.info("[WebSocketNotificationChannel] Simulated WebSocket message to {}: {}",
                notification.getRecipient().getUsername(),
                notification.getTitle());
        // Implement actual websocket push here if needed
        return true;
    }
}