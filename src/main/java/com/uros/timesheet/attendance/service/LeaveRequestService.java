package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.leave.LeaveRequestCreateRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

public interface LeaveRequestService {
    LeaveRequestResponse createRequest(LeaveRequestCreateRequest request);
    LeaveRequestResponse submitRequest(UUID id, CustomUserDetails principal);
    LeaveRequestResponse approveRequest(UUID id, UUID approverId);
    LeaveRequestResponse rejectRequest(UUID id, UUID approverId, String reason);
    LeaveRequestResponse getRequestById(UUID id);
    List<LeaveRequestResponse> getRequestsForUser(UUID userId);

    LeaveRequestResponse softDeleteRequest(UUID id, UUID performedByUserId, String reason);
    LeaveRequestResponse restoreRequest(UUID id, UUID performedByUserId, String reason);

    List<LeaveRequestResponse> getRequestsForCurrentTenant();
}