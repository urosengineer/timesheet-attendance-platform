package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.enums.NotificationType;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNotificationHelper {

    private final NotificationService notificationService;
    private final MessageUtil messageUtil;

    public void sendSoftDeleteNotification(User user, String reason) {
        NotificationCreateRequest notification = new NotificationCreateRequest();
        notification.setRecipientId(user.getId());
        notification.setType(NotificationType.WEBSOCKET);
        notification.setTitle(messageUtil.get("notification.user.softdelete.title", user.getUsername()));
        notification.setMessage(messageUtil.get("notification.user.softdelete.body", user.getUsername(), reason));
        notificationService.createAndSend(notification);
    }

    public void sendRestoreNotification(User user, String reason) {
        NotificationCreateRequest notification = new NotificationCreateRequest();
        notification.setRecipientId(user.getId());
        notification.setType(NotificationType.WEBSOCKET);
        notification.setTitle(messageUtil.get("notification.user.restore.title", user.getUsername()));
        notification.setMessage(messageUtil.get("notification.user.restore.body", user.getUsername(), reason));
        notificationService.createAndSend(notification);
    }
}