package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.domain.Organization;
import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestCreateRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.event.LeaveRequestStatusChangedEvent;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.LeaveRequestMapper;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.repository.OrganizationRepository;
import com.uros.timesheet.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.uros.timesheet.attendance.exception.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveRequestCreateHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final MessageUtil messageUtil;
    private final DomainEventPublisher eventPublisher;

    public LeaveRequestResponse handle(LeaveRequestCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("error.user.not.found"));
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.organization.not.found")));

        LeaveRequest entity = LeaveRequest.builder()
                .user(user)
                .organization(organization)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .type(request.getType())
                .status("DRAFT")
                .notes(request.getNotes())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        leaveRequestRepository.save(entity);

        eventPublisher.publish(new LeaveRequestStatusChangedEvent(
                this,
                entity.getId(),
                user.getId(),
                "NONE",
                "DRAFT",
                user.getId(),
                null,
                entity.getCreatedAt()
        ));

        return leaveRequestMapper.toResponse(entity);
    }
}