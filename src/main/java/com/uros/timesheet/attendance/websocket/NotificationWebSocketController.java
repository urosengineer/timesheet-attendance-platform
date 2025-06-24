package com.uros.timesheet.attendance.websocket;

import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket controller for delivering notifications in real-time.
 * Supports both user-specific and broadcast messaging.
 */
@RestController
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send a notification to a specific user.
     * The frontend should subscribe to /user/queue/notifications or /queue/notifications/{userId}
     *
     * @param notification Notification payload
     * @param userId       UUID string of the recipient
     */
    public void sendNotificationToUser(NotificationResponse notification, String userId) {
        messagingTemplate.convertAndSend("/queue/notifications/" + userId, notification);
        // Alternativno: messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * Broadcasts a demo notification message to all subscribers.
     * The frontend should subscribe to /topic/notifications.
     *
     * @param message Message to broadcast
     * @return       The same message, as demo
     */
    @MessageMapping("/notify")
    public void broadcastNotification(String message) {
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}