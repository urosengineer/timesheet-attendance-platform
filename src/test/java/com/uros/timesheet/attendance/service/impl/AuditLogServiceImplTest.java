package com.uros.timesheet.attendance.auditlog;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock UserRepository userRepository;
    @Mock AuditLogMapper auditLogMapper;

    @InjectMocks
    AuditLogServiceImpl auditLogService;

    @Test
    void log_shouldSaveWithUser_whenUserIdProvided() {
        UUID userId = UUID.randomUUID();
        String eventType = "LOGIN";
        String details = "User login successful";

        User user = User.builder().id(userId).username("user").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        auditLogService.log(eventType, userId, details, "1.2.3.4", "JUnit");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();

        assertThat(log.getEventType()).isEqualTo(eventType);
        assertThat(log.getUser()).isSameAs(user);
        assertThat(log.getDetails()).isEqualTo(details);
        assertThat(log.getIpAddress()).isEqualTo("1.2.3.4");
        assertThat(log.getUserAgent()).isEqualTo("JUnit");
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void log_shouldSaveWithoutUser_whenUserIdNull() {
        String eventType = "SYSTEM_EVENT";
        String details = "System maintenance";

        when(auditLogRepository.save(any(AuditLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        auditLogService.log(eventType, null, details, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();

        assertThat(log.getEventType()).isEqualTo(eventType);
        assertThat(log.getUser()).isNull();
        assertThat(log.getDetails()).isEqualTo(details);
        assertThat(log.getIpAddress()).isNull();
        assertThat(log.getUserAgent()).isNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    void log_overload_calls_main_log() {
        AuditLogServiceImpl spyService = Mockito.spy(auditLogService);
        doNothing().when(spyService)
                .log(eq("event"), isNull(), eq("details"), isNull(), isNull());

        spyService.log("event", null, "details");

        verify(spyService).log("event", null, "details", null, null);
    }

    @Test
    void getLogsForUser_shouldReturnMappedResponses() {
        UUID userId = UUID.randomUUID();
        AuditLog log1 = AuditLog.builder().id(UUID.randomUUID()).build();
        AuditLog log2 = AuditLog.builder().id(UUID.randomUUID()).build();
        when(auditLogRepository.findByUserId(userId)).thenReturn(List.of(log1, log2));

        AuditLogResponse resp1 = new AuditLogResponse();
        AuditLogResponse resp2 = new AuditLogResponse();
        when(auditLogMapper.toResponse(log1)).thenReturn(resp1);
        when(auditLogMapper.toResponse(log2)).thenReturn(resp2);

        List<AuditLogResponse> results = auditLogService.getLogsForUser(userId);
        assertThat(results).containsExactly(resp1, resp2);
    }

    @Test
    void getLogsByEventType_shouldReturnMappedResponses() {
        String eventType = "UPDATE_USER";
        AuditLog log = AuditLog.builder().id(UUID.randomUUID()).build();
        when(auditLogRepository.findByEventType(eventType)).thenReturn(List.of(log));

        AuditLogResponse resp = new AuditLogResponse();
        when(auditLogMapper.toResponse(log)).thenReturn(resp);

        List<AuditLogResponse> results = auditLogService.getLogsByEventType(eventType);
        assertThat(results).containsExactly(resp);
    }

    @Test
    void getAll_shouldReturnMappedResponses() {
        AuditLog log = AuditLog.builder().id(UUID.randomUUID()).build();
        when(auditLogRepository.findAll()).thenReturn(List.of(log));
        AuditLogResponse resp = new AuditLogResponse();
        when(auditLogMapper.toResponse(log)).thenReturn(resp);

        List<AuditLogResponse> results = auditLogService.getAll();
        assertThat(results).containsExactly(resp);
    }

    @Test
    void getLogs_shouldDelegateToFindByEventTypeAndUserId_whenBothPresent() {
        String eventType = "CREATE";
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<AuditLog> page = new PageImpl<>(List.of());
        when(auditLogRepository.findByEventTypeAndUserId(eventType, userId, pageable)).thenReturn(page);

        auditLogService.getLogs(0, 10, eventType, userId);

        verify(auditLogRepository).findByEventTypeAndUserId(eventType, userId, pageable);
    }

    @Test
    void getLogs_shouldDelegateToFindByEventType_whenOnlyEventType() {
        String eventType = "DELETE";
        Pageable pageable = PageRequest.of(0, 5);
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(auditLogRepository.findByEventType(eventType, pageable)).thenReturn(page);

        auditLogService.getLogs(0, 5, eventType, null);

        verify(auditLogRepository).findByEventType(eventType, pageable);
    }

    @Test
    void getLogs_shouldDelegateToFindByUserId_whenOnlyUserId() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 20);
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(auditLogRepository.findByUserId(userId, pageable)).thenReturn(page);

        auditLogService.getLogs(1, 20, null, userId);

        verify(auditLogRepository).findByUserId(userId, pageable);
    }

    @Test
    void getLogs_shouldDelegateToFindAll_whenNoFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLog> page = new PageImpl<>(List.of());
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        auditLogService.getLogs(0, 10, null, null);

        verify(auditLogRepository).findAll(pageable);
    }
}
