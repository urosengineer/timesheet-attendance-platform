package com.uros.timesheet.attendance.notification;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DummyNotificationChannel implements NotificationChannel {

    @Override
    public NotificationType getType() {
        return NotificationType.DUMMY;
    }

    @Override
    public boolean send(Notification notification) {
        log.info("[DummyNotificationChannel] Simulated send '{}' to {}: {}",
                notification.getType(),
                notification.getRecipient().getEmail(),
                notification.getTitle());
        return true;
    }
}