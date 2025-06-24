package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.dto.leave.LeaveRequestCreateRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.service.LeaveRequestService;
import com.uros.timesheet.attendance.service.handler.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestCreateHandler createHandler;
    private final LeaveRequestSubmitHandler submitHandler;
    private final LeaveRequestApproveHandler approveHandler;
    private final LeaveRequestRejectHandler rejectHandler;
    private final LeaveRequestDeleteHandler deleteHandler;
    private final LeaveRequestRestoreHandler restoreHandler;
    private final LeaveRequestQueryHandler queryHandler;

    @Override
    @Transactional
    public LeaveRequestResponse createRequest(LeaveRequestCreateRequest request) {
        return createHandler.handle(request);
    }

    @Override
    @Transactional
    public LeaveRequestResponse submitRequest(UUID id, CustomUserDetails principal) {
        return submitHandler.handle(id, principal);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approveRequest(UUID id, UUID approverId) {
        return approveHandler.handle(id, approverId);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectRequest(UUID id, UUID approverId, String reason) {
        return rejectHandler.handle(id, approverId, reason);
    }

    @Override
    @Transactional
    public LeaveRequestResponse softDeleteRequest(UUID id, UUID performedByUserId, String reason) {
        return deleteHandler.handle(id, performedByUserId, reason);
    }

    @Override
    @Transactional
    public LeaveRequestResponse restoreRequest(UUID id, UUID performedByUserId, String reason) {
        return restoreHandler.handle(id, performedByUserId, reason);
    }

    @Override
    public LeaveRequestResponse getRequestById(UUID id) {
        return queryHandler.getById(id);
    }

    @Override
    public List<LeaveRequestResponse> getRequestsForUser(UUID userId) {
        return queryHandler.getByUser(userId);
    }

    @Override
    public List<LeaveRequestResponse> getRequestsForCurrentTenant() {
        return queryHandler.getByTenant();
    }
}