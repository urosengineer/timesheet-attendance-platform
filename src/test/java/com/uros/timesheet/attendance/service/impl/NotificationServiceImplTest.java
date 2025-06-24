package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.Notification;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.notification.NotificationCreateRequest;
import com.uros.timesheet.attendance.dto.notification.NotificationResponse;
import com.uros.timesheet.attendance.enums.NotificationType;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.NotificationMapper;
import com.uros.timesheet.attendance.notification.NotificationChannel;
import com.uros.timesheet.attendance.notification.NotificationChannelRegistry;
import com.uros.timesheet.attendance.repository.NotificationRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.service.helper.NotificationMetricHelper;
import com.uros.timesheet.attendance.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = NotificationServiceImpl.class)
class NotificationServiceImplTest {

    @MockBean private NotificationRepository notificationRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private NotificationMapper notificationMapper;
    @MockBean private MessageUtil messageUtil;
    @MockBean private NotificationChannelRegistry notificationChannelRegistry;
    @MockBean private NotificationMetricHelper notificationMetricHelper;

    @Autowired
    private NotificationServiceImpl notificationService;

    private NotificationCreateRequest validRequest;
    private User recipient;
    private Notification notification;
    private NotificationResponse response;

    @BeforeEach
    void setUp() {
        // Validni podaci
        recipient = new User();
        recipient.setId(UUID.randomUUID());
        recipient.setEmail("test@user.com");

        validRequest = new NotificationCreateRequest();
        validRequest.setRecipientId(recipient.getId());
        validRequest.setType(NotificationType.EMAIL);
        validRequest.setTitle("Test Title");
        validRequest.setMessage("Test message");

        notification = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .type(NotificationType.EMAIL)
                .title("Test Title")
                .message("Test message")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        response = NotificationResponse.builder()
                .id(notification.getId())
                .recipient(null) // UserResponse se može mockovati ako treba
                .type(NotificationType.EMAIL)
                .title("Test Title")
                .message("Test message")
                .status("SENT")
                .createdAt(notification.getCreatedAt())
                .sentAt(Instant.now())
                .build();

        // Metrics: startSample vraća dummy objekt (može biti null, samo se propagira)
        when(notificationMetricHelper.startSample()).thenReturn(null);

        // Default: na metrics metode ne radimo ništa
        doNothing().when(notificationMetricHelper).incrementTotal(anyString());
        doNothing().when(notificationMetricHelper).incrementStatus(anyString(), anyBoolean());
        doNothing().when(notificationMetricHelper).recordLatency(anyString(), anyBoolean(), any());

        // Default: repository save vraća objekat koji mu je prosleđen
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createAndSend_successfullySendsNotification() {
        NotificationChannel channel = mock(NotificationChannel.class);
        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        when(notificationChannelRegistry.getChannel(NotificationType.EMAIL)).thenReturn(channel);
        when(channel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(response);

        NotificationResponse result = notificationService.createAndSend(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("SENT");

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(channel, times(1)).send(any(Notification.class));
        verify(notificationMetricHelper).incrementTotal("EMAIL");
        verify(notificationMetricHelper).incrementStatus("EMAIL", true);
        verify(notificationMetricHelper).recordLatency(eq("EMAIL"), eq(true), any());
    }

    @Test
    void createAndSend_channelFailsToSend_statusFailed() {
        NotificationChannel channel = mock(NotificationChannel.class);
        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        when(notificationChannelRegistry.getChannel(NotificationType.EMAIL)).thenReturn(channel);
        when(channel.send(any(Notification.class))).thenReturn(false);

        Notification failedNotification = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .type(NotificationType.EMAIL)
                .title("Fail Title")
                .message("Fail Message")
                .status("FAILED")
                .createdAt(Instant.now())
                .build();

        NotificationResponse failedResponse = NotificationResponse.builder()
                .id(failedNotification.getId())
                .type(NotificationType.EMAIL)
                .title("Fail Title")
                .message("Fail Message")
                .status("FAILED")
                .createdAt(failedNotification.getCreatedAt())
                .build();

        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(failedResponse);

        NotificationCreateRequest failRequest = new NotificationCreateRequest();
        failRequest.setRecipientId(recipient.getId());
        failRequest.setType(NotificationType.EMAIL);
        failRequest.setTitle("Fail Title");
        failRequest.setMessage("Fail Message");

        NotificationResponse result = notificationService.createAndSend(failRequest);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        verify(notificationMetricHelper).incrementStatus("EMAIL", false);
    }

    @Test
    void createAndSend_channelNotFound_statusFailed() {
        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
        when(notificationChannelRegistry.getChannel(NotificationType.DUMMY)).thenReturn(null);

        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(recipient.getId());
        request.setType(NotificationType.DUMMY);
        request.setTitle("No channel");
        request.setMessage("Should warn!");

        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .type(NotificationType.DUMMY)
                .title("No channel")
                .message("Should warn!")
                .status("FAILED")
                .createdAt(Instant.now())
                .build();

        NotificationResponse failedResponse = NotificationResponse.builder()
                .id(notification.getId())
                .type(NotificationType.DUMMY)
                .title("No channel")
                .message("Should warn!")
                .status("FAILED")
                .createdAt(notification.getCreatedAt())
                .build();

        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(failedResponse);

        NotificationResponse result = notificationService.createAndSend(request);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        verify(notificationMetricHelper).incrementStatus("DUMMY", false);
        // Pošto je channel null, log.warn bi trebao da se dogodi (ne može se lako assert-ovati bez log spy).
    }

    @Test
    void createAndSend_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(UUID.randomUUID());
        request.setType(NotificationType.EMAIL);
        request.setTitle("Any title");
        request.setMessage("Any message");

        assertThatThrownBy(() -> notificationService.createAndSend(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.user.not.found");
    }

    @Test
    void getById_returnsNotificationResponse() {
        UUID notifId = UUID.randomUUID();
        Notification n = Notification.builder()
                .id(notifId)
                .recipient(recipient)
                .type(NotificationType.EMAIL)
                .title("title")
                .message("msg")
                .status("SENT")
                .createdAt(Instant.now())
                .build();

        NotificationResponse r = NotificationResponse.builder()
                .id(notifId)
                .type(NotificationType.EMAIL)
                .title("title")
                .message("msg")
                .status("SENT")
                .createdAt(n.getCreatedAt())
                .build();

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(n));
        when(notificationMapper.toResponse(n)).thenReturn(r);

        NotificationResponse result = notificationService.getById(notifId);
        assertThat(result).isEqualTo(r);
    }

    @Test
    void getById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());
        when(messageUtil.get("error.notification.not.found")).thenReturn("not found");

        assertThatThrownBy(() -> notificationService.getById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.notification.not.found");
    }

    @Test
    void getForRecipient_returnsListOfNotifications() {
        UUID recipientId = recipient.getId();
        Notification n1 = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .type(NotificationType.EMAIL)
                .status("SENT")
                .createdAt(Instant.now())
                .build();
        Notification n2 = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .type(NotificationType.WEBSOCKET)
                .status("SENT")
                .createdAt(Instant.now())
                .build();
        NotificationResponse r1 = NotificationResponse.builder()
                .id(n1.getId())
                .type(NotificationType.EMAIL)
                .status("SENT")
                .createdAt(n1.getCreatedAt())
                .build();
        NotificationResponse r2 = NotificationResponse.builder()
                .id(n2.getId())
                .type(NotificationType.WEBSOCKET)
                .status("SENT")
                .createdAt(n2.getCreatedAt())
                .build();

        when(notificationRepository.findByRecipientId(eq(recipientId), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(n1, n2)));
        when(notificationMapper.toResponse(n1)).thenReturn(r1);
        when(notificationMapper.toResponse(n2)).thenReturn(r2);

        List<NotificationResponse> list = notificationService.getForRecipient(recipientId);
        assertThat(list).containsExactly(r1, r2);
    }
}
