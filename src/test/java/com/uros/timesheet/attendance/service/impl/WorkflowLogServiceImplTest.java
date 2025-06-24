package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.domain.WorkflowLog;
import com.uros.timesheet.attendance.dto.user.UserResponse;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogCreateRequest;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogResponse;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.WorkflowLogMapper;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.repository.WorkflowLogRepository;
import com.uros.timesheet.attendance.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class WorkflowLogServiceImplTest {

    @Mock
    private WorkflowLogRepository workflowLogRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkflowLogMapper workflowLogMapper;
    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private WorkflowLogServiceImpl workflowLogService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).username("tester").build();
    }

    @Test
    void logTransition_success() {
        // Arrange
        UUID entityId = UUID.randomUUID();
        WorkflowLogCreateRequest request = WorkflowLogCreateRequest.builder()
                .relatedEntityType("AttendanceRecord")
                .relatedEntityId(entityId)
                .oldStatus("PENDING")
                .newStatus("APPROVED")
                .userId(userId)
                .comment("Approved by admin")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ArgumentCaptor<WorkflowLog> logCaptor = ArgumentCaptor.forClass(WorkflowLog.class);

        WorkflowLog savedLog = WorkflowLog.builder()
                .id(UUID.randomUUID())
                .relatedEntityType("AttendanceRecord")
                .relatedEntityId(entityId)
                .oldStatus("PENDING")
                .newStatus("APPROVED")
                .user(user)
                .timestamp(Instant.now())
                .comment("Approved by admin")
                .build();

        when(workflowLogRepository.save(any(WorkflowLog.class))).thenReturn(savedLog);

        WorkflowLogResponse expectedResponse = new WorkflowLogResponse();
        expectedResponse.setId(savedLog.getId());
        expectedResponse.setRelatedEntityType("AttendanceRecord");
        expectedResponse.setRelatedEntityId(entityId);
        expectedResponse.setOldStatus("PENDING");
        expectedResponse.setNewStatus("APPROVED");
        expectedResponse.setComment("Approved by admin");
        expectedResponse.setTimestamp(savedLog.getTimestamp());

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userId);
        userResponse.setUsername("tester");
        expectedResponse.setUser(userResponse);

        when(workflowLogMapper.toResponse(any(WorkflowLog.class))).thenReturn(expectedResponse);

        // Act
        WorkflowLogResponse actualResponse = workflowLogService.logTransition(request);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getRelatedEntityType()).isEqualTo("AttendanceRecord");
        assertThat(actualResponse.getNewStatus()).isEqualTo("APPROVED");
        verify(userRepository).findById(userId);
        verify(workflowLogRepository).save(logCaptor.capture());
        WorkflowLog captured = logCaptor.getValue();
        assertThat(captured.getRelatedEntityType()).isEqualTo("AttendanceRecord");
        assertThat(captured.getUser()).isEqualTo(user);
        verify(workflowLogMapper).toResponse(any(WorkflowLog.class));
    }

    @Test
    void logTransition_throws_whenUserNotFound() {
        // Arrange
        UUID entityId = UUID.randomUUID();
        WorkflowLogCreateRequest request = WorkflowLogCreateRequest.builder()
                .relatedEntityType("AttendanceRecord")
                .relatedEntityId(entityId)
                .oldStatus("PENDING")
                .newStatus("APPROVED")
                .userId(userId)
                .comment("Test")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> workflowLogService.logTransition(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.user.not.found");
        verify(userRepository).findById(userId);
        verifyNoInteractions(workflowLogRepository, workflowLogMapper);
    }

    @Test
    void getLogsForEntity_success() {
        // Arrange
        UUID entityId = UUID.randomUUID();
        String entityType = "AttendanceRecord";
        WorkflowLog log1 = WorkflowLog.builder().id(UUID.randomUUID()).relatedEntityType(entityType).relatedEntityId(entityId).build();
        WorkflowLog log2 = WorkflowLog.builder().id(UUID.randomUUID()).relatedEntityType(entityType).relatedEntityId(entityId).build();

        List<WorkflowLog> logs = List.of(log1, log2);

        when(workflowLogRepository.findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId)).thenReturn(logs);

        WorkflowLogResponse resp1 = new WorkflowLogResponse();
        resp1.setId(log1.getId());
        WorkflowLogResponse resp2 = new WorkflowLogResponse();
        resp2.setId(log2.getId());

        when(workflowLogMapper.toResponse(log1)).thenReturn(resp1);
        when(workflowLogMapper.toResponse(log2)).thenReturn(resp2);

        // Act
        List<WorkflowLogResponse> result = workflowLogService.getLogsForEntity(entityType, entityId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(log1.getId());
        assertThat(result.get(1).getId()).isEqualTo(log2.getId());
        verify(workflowLogRepository).findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId);
    }

    @Test
    void getLogsForEntity_throwsIfEmpty() {
        UUID entityId = UUID.randomUUID();
        String entityType = "AttendanceRecord";
        when(workflowLogRepository.findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId)).thenReturn(List.of());
        when(messageUtil.get("error.workflowlog.not.found")).thenReturn("No logs found");

        assertThatThrownBy(() -> workflowLogService.getLogsForEntity(entityType, entityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("No logs found");
        verify(messageUtil).get("error.workflowlog.not.found");
    }
}
