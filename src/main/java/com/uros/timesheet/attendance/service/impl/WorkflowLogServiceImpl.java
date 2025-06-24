package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.domain.WorkflowLog;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogCreateRequest;
import com.uros.timesheet.attendance.dto.workflow.WorkflowLogResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.WorkflowLogMapper;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.repository.WorkflowLogRepository;
import com.uros.timesheet.attendance.service.WorkflowLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.uros.timesheet.attendance.exception.NotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkflowLogServiceImpl implements WorkflowLogService {

    private final WorkflowLogRepository workflowLogRepository;
    private final UserRepository userRepository;
    private final WorkflowLogMapper workflowLogMapper;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public WorkflowLogResponse logTransition(WorkflowLogCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));
        WorkflowLog log = WorkflowLog.builder()
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .oldStatus(request.getOldStatus())
                .newStatus(request.getNewStatus())
                .user(user)
                .timestamp(Instant.now())
                .comment(request.getComment())
                .build();
        workflowLogRepository.save(log);
        return workflowLogMapper.toResponse(log);
    }

    @Override
    public List<WorkflowLogResponse> getLogsForEntity(String entityType, UUID entityId) {
        List<WorkflowLog> logs = workflowLogRepository.findByRelatedEntityTypeAndRelatedEntityId(entityType, entityId);
        if (logs.isEmpty()) {
            throw new NotFoundException(messageUtil.get("error.workflowlog.not.found"));
        }
        return logs.stream().map(workflowLogMapper::toResponse).toList();
    }
}