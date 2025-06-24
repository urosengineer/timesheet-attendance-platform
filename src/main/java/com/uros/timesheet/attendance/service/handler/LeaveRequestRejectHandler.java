package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.domain.Role;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.event.LeaveRequestStatusChangedEvent;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.LeaveRequestMapper;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import com.uros.timesheet.attendance.workflow.WorkflowEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.exception.WorkflowTransitionDeniedException;


import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LeaveRequestRejectHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final WorkflowEngineService workflowEngineService;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;

    public LeaveRequestResponse handle(UUID id, UUID approverId, String reason) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.leaverequest.not.found")));

        if (entity.getUser().getId().equals(approverId)) {
            throw new IllegalArgumentException(messageUtil.get("error.leaverequest.reject.self"));
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));
        Set<String> approverRoles = approver.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        if (!workflowEngineService.canTransition(
                "LeaveRequest",
                entity.getStatus(),
                "REJECTED",
                approverRoles
        )) {
            throw new WorkflowTransitionDeniedException("error.workflow.transition.denied");
        }

        String oldStatus = entity.getStatus();
        entity.setStatus("REJECTED");
        entity.setApprover(approver);
        entity.setNotes(reason);
        entity.setUpdatedAt(Instant.now());
        leaveRequestRepository.save(entity);

        eventPublisher.publish(new LeaveRequestStatusChangedEvent(
                this,
                entity.getId(),
                entity.getUser().getId(),
                oldStatus,
                entity.getStatus(),
                approverId,
                reason,
                entity.getUpdatedAt()
        ));

        return leaveRequestMapper.toResponse(entity);
    }
}