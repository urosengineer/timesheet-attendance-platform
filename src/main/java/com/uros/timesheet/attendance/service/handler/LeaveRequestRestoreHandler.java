package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.domain.LeaveRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.event.DomainEventPublisher;
import com.uros.timesheet.attendance.event.LeaveRequestStatusChangedEvent;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.LeaveRequestMapper;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveRequestRestoreHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final DomainEventPublisher eventPublisher;
    private final MessageUtil messageUtil;

    public LeaveRequestResponse handle(UUID id, UUID performedByUserId, String reason) {
        LeaveRequest entity = leaveRequestRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException(messageUtil.get("error.leaverequest.not.found")));
        if (!entity.isDeleted()) {
            throw new IllegalStateException(messageUtil.get("error.leaverequest.not.deleted"));
        }
        entity.restore();
        entity.setUpdatedAt(Instant.now());
        leaveRequestRepository.save(entity);

        eventPublisher.publish(new LeaveRequestStatusChangedEvent(
                this,
                entity.getId(),
                entity.getUser().getId(),
                "DELETED",
                entity.getStatus(),
                performedByUserId,
                reason,
                entity.getUpdatedAt()
        ));
        return leaveRequestMapper.toResponse(entity);
    }
}