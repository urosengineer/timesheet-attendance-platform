package com.uros.timesheet.attendance.mapper;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NotificationMapper {
    NotificationResponse toResponse(Notification entity);
}