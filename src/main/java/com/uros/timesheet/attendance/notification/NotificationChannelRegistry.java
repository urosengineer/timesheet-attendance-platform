package com.uros.timesheet.attendance.notification;

import com.uros.timesheet.attendance.enums.NotificationType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for all NotificationChannel implementations, used to route notifications
 * based on channel type.
 */
@Component
@RequiredArgsConstructor
public class NotificationChannelRegistry {

    private final List<NotificationChannel> channels;
    private final Map<NotificationType, NotificationChannel> typeToChannel = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NotificationChannel channel : channels) {
            typeToChannel.put(channel.getType(), channel);
        }
    }

    public NotificationChannel getChannel(NotificationType type) {
        return typeToChannel.get(type);
    }
}