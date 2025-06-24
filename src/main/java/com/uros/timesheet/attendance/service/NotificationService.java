package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    NotificationResponse createAndSend(NotificationCreateRequest request);
    NotificationResponse getById(UUID id);
    List<NotificationResponse> getForRecipient(UUID recipientId);

    Page<NotificationResponse> getForRecipientPaginated(UUID recipientId, Pageable pageable);
}