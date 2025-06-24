package com.uros.timesheet.attendance.notification;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.enums.NotificationType;

public interface NotificationChannel {
    NotificationType getType();
    boolean send(Notification notification);
}