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
import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.workflow.WorkflowEngineService;
import com.uros.timesheet.attendance.exception.WorkflowTransitionDeniedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LeaveRequestSubmitHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final WorkflowEngineService workflowEngineService;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;

    public LeaveRequestResponse handle(UUID id, CustomUserDetails principal) {
        LeaveRequest entity = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.leaverequest.not.found")));

        boolean isOwner = entity.getUser().getId().equals(principal.getId());
        boolean isAdmin = principal.getRoleNames().contains("ADMIN");
        boolean hasSubmitPermission = principal.getPermissionNames().contains("LEAVE_REQUEST_SUBMIT");

        if (!(isOwner || isAdmin || hasSubmitPermission)) {
            throw new AccessDeniedException(messageUtil.get("error.leaverequest.unauthorized"));
        }

        Set<String> actingRoles = isAdmin || hasSubmitPermission
                ? principal.getRoleNames()
                : entity.getUser().getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        if (!workflowEngineService.canTransition(
                "LeaveRequest",
                entity.getStatus(),
                "SUBMITTED",
                actingRoles
        )) {
            throw new WorkflowTransitionDeniedException("error.workflow.transition.denied");
        }

        String oldStatus = entity.getStatus();
        entity.setStatus("SUBMITTED");
        entity.setUpdatedAt(Instant.now());
        leaveRequestRepository.save(entity);

        eventPublisher.publish(new LeaveRequestStatusChangedEvent(
                this,
                entity.getId(),
                entity.getUser().getId(),
                oldStatus,
                entity.getStatus(),
                principal.getId(),
                null,
                entity.getUpdatedAt()
        ));

        return leaveRequestMapper.toResponse(entity);
    }
}