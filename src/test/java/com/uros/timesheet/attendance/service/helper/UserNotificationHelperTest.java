package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.enums.NotificationType;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserNotificationHelper.
 * Covers notification content and interaction with the NotificationService.
 */
class UserNotificationHelperTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserNotificationHelper helper;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("uros.test");
    }

    @Test
    void sendSoftDeleteNotification_sendsNotificationWithCorrectContent() {
        // Arrange
        String reason = "Violation of policy";
        when(messageUtil.get("notification.user.softdelete.title", user.getUsername()))
                .thenReturn("Account Deactivated");
        when(messageUtil.get("notification.user.softdelete.body", user.getUsername(), reason))
                .thenReturn("Your account has been deactivated. Reason: Violation of policy");

        // Act
        helper.sendSoftDeleteNotification(user, reason);

        // Assert
        ArgumentCaptor<NotificationCreateRequest> captor = ArgumentCaptor.forClass(NotificationCreateRequest.class);
        verify(notificationService).createAndSend(captor.capture());

        NotificationCreateRequest notification = captor.getValue();
        assertNotNull(notification);
        assertEquals(user.getId(), notification.getRecipientId());
        assertEquals(NotificationType.WEBSOCKET, notification.getType());
        assertEquals("Account Deactivated", notification.getTitle());
        assertEquals("Your account has been deactivated. Reason: Violation of policy", notification.getMessage());
    }

    @Test
    void sendRestoreNotification_sendsNotificationWithCorrectContent() {
        // Arrange
        String reason = "Appeal granted";
        when(messageUtil.get("notification.user.restore.title", user.getUsername()))
                .thenReturn("Account Restored");
        when(messageUtil.get("notification.user.restore.body", user.getUsername(), reason))
                .thenReturn("Your account has been restored. Reason: Appeal granted");

        // Act
        helper.sendRestoreNotification(user, reason);

        // Assert
        ArgumentCaptor<NotificationCreateRequest> captor = ArgumentCaptor.forClass(NotificationCreateRequest.class);
        verify(notificationService).createAndSend(captor.capture());

        NotificationCreateRequest notification = captor.getValue();
        assertNotNull(notification);
        assertEquals(user.getId(), notification.getRecipientId());
        assertEquals(NotificationType.WEBSOCKET, notification.getType());
        assertEquals("Account Restored", notification.getTitle());
        assertEquals("Your account has been restored. Reason: Appeal granted", notification.getMessage());
    }
}
