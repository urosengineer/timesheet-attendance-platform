package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.NotificationMapper;
import com.uros.timesheet.attendance.notification.NotificationChannel;
import com.uros.timesheet.attendance.notification.NotificationChannelRegistry;
import com.uros.timesheet.attendance.repository.NotificationRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.NotificationService;
import com.uros.timesheet.attendance.service.helper.NotificationMetricHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.uros.timesheet.attendance.exception.NotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final MessageUtil messageUtil;
    private final NotificationChannelRegistry notificationChannelRegistry;
    private final NotificationMetricHelper notificationMetricHelper;

    @Override
    @Transactional
    public NotificationResponse createAndSend(NotificationCreateRequest request) {
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        notificationRepository.save(notification);

        var timerSample = notificationMetricHelper.startSample();

        NotificationChannel channel = notificationChannelRegistry.getChannel(request.getType());
        boolean sentSuccessfully = false;
        if (channel != null) {
            sentSuccessfully = channel.send(notification);
        } else {
            log.warn("[NotificationService] No channel found for type '{}'", request.getType());
        }

        String type = request.getType() != null ? request.getType().name() : "UNKNOWN";
        notificationMetricHelper.incrementTotal(type);
        notificationMetricHelper.incrementStatus(type, sentSuccessfully);
        notificationMetricHelper.recordLatency(type, sentSuccessfully, timerSample);

        notification.setStatus(sentSuccessfully ? "SENT" : "FAILED");
        notification.setSentAt(sentSuccessfully ? Instant.now() : null);
        notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse getById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("error.notification.not.found"));
        return notificationMapper.toResponse(notification);
    }

    @Override
    public List<NotificationResponse> getForRecipient(UUID recipientId) {
        List<Notification> list = notificationRepository.findByRecipientId(recipientId, Pageable.unpaged()).getContent();
        return list.stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    public Page<NotificationResponse> getForRecipientPaginated(UUID recipientId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByRecipientId(recipientId, pageable);
        return page.map(notificationMapper::toResponse);
    }
}