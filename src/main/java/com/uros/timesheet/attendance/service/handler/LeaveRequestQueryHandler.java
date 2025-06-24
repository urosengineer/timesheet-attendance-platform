package com.uros.timesheet.attendance.service.handler;

import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.mapper.LeaveRequestMapper;
import com.uros.timesheet.attendance.repository.LeaveRequestRepository;
import com.uros.timesheet.attendance.util.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveRequestQueryHandler {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveRequestMapper leaveRequestMapper;
    private final MessageUtil messageUtil;

    public LeaveRequestResponse getById(UUID id) {
        return leaveRequestRepository.findById(id)
                .map(leaveRequestMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageUtil.get("error.leaverequest.not.found")
                ));
    }

    public List<LeaveRequestResponse> getByUser(UUID userId) {
        return leaveRequestRepository.findByUserId(userId).stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }

    public List<LeaveRequestResponse> getByTenant() {
        String tenantIdString = TenantContext.getTenantId();
        if (tenantIdString == null)
            throw new IllegalStateException(messageUtil.get("error.tenant.not.set"));
        UUID tenantId = UUID.fromString(tenantIdString);
        return leaveRequestRepository.findByOrganizationId(tenantId).stream()
                .map(leaveRequestMapper::toResponse)
                .toList();
    }
}