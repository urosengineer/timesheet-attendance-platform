package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.dto.leave.LeaveRequestCreateRequest;
import com.uros.timesheet.attendance.dto.leave.LeaveRequestResponse;
import com.uros.timesheet.attendance.security.CustomUserDetails;
import com.uros.timesheet.attendance.service.handler.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {LeaveRequestServiceImpl.class})
class LeaveRequestServiceImplTest {

    @MockBean private LeaveRequestCreateHandler createHandler;
    @MockBean private LeaveRequestSubmitHandler submitHandler;
    @MockBean private LeaveRequestApproveHandler approveHandler;
    @MockBean private LeaveRequestRejectHandler rejectHandler;
    @MockBean private LeaveRequestDeleteHandler deleteHandler;
    @MockBean private LeaveRequestRestoreHandler restoreHandler;
    @MockBean private LeaveRequestQueryHandler queryHandler;

    @Autowired
    private LeaveRequestServiceImpl service;

    @Test
    void createRequest_delegatesToHandler_andReturnsResponse() {
        LeaveRequestCreateRequest req = new LeaveRequestCreateRequest();
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(createHandler.handle(req)).thenReturn(expected);

        LeaveRequestResponse result = service.createRequest(req);

        assertThat(result).isSameAs(expected);
        verify(createHandler).handle(req);
    }

    @Test
    void submitRequest_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        CustomUserDetails principal = mock(CustomUserDetails.class);
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(submitHandler.handle(id, principal)).thenReturn(expected);

        LeaveRequestResponse result = service.submitRequest(id, principal);

        assertThat(result).isSameAs(expected);
        verify(submitHandler).handle(id, principal);
    }

    @Test
    void approveRequest_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(approveHandler.handle(id, approverId)).thenReturn(expected);

        LeaveRequestResponse result = service.approveRequest(id, approverId);

        assertThat(result).isSameAs(expected);
        verify(approveHandler).handle(id, approverId);
    }

    @Test
    void rejectRequest_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        String reason = "Reason";
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(rejectHandler.handle(id, approverId, reason)).thenReturn(expected);

        LeaveRequestResponse result = service.rejectRequest(id, approverId, reason);

        assertThat(result).isSameAs(expected);
        verify(rejectHandler).handle(id, approverId, reason);
    }

    @Test
    void softDeleteRequest_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String reason = "delete reason";
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(deleteHandler.handle(id, userId, reason)).thenReturn(expected);

        LeaveRequestResponse result = service.softDeleteRequest(id, userId, reason);

        assertThat(result).isSameAs(expected);
        verify(deleteHandler).handle(id, userId, reason);
    }

    @Test
    void restoreRequest_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String reason = "restore reason";
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(restoreHandler.handle(id, userId, reason)).thenReturn(expected);

        LeaveRequestResponse result = service.restoreRequest(id, userId, reason);

        assertThat(result).isSameAs(expected);
        verify(restoreHandler).handle(id, userId, reason);
    }

    @Test
    void getRequestById_delegatesToQueryHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        LeaveRequestResponse expected = new LeaveRequestResponse();

        when(queryHandler.getById(id)).thenReturn(expected);

        LeaveRequestResponse result = service.getRequestById(id);

        assertThat(result).isSameAs(expected);
        verify(queryHandler).getById(id);
    }

    @Test
    void getRequestsForUser_delegatesToQueryHandler_andReturnsList() {
        UUID userId = UUID.randomUUID();
        LeaveRequestResponse resp1 = new LeaveRequestResponse();
        LeaveRequestResponse resp2 = new LeaveRequestResponse();

        when(queryHandler.getByUser(userId)).thenReturn(List.of(resp1, resp2));

        List<LeaveRequestResponse> result = service.getRequestsForUser(userId);

        assertThat(result).containsExactly(resp1, resp2);
        verify(queryHandler).getByUser(userId);
    }

    @Test
    void getRequestsForCurrentTenant_delegatesToQueryHandler_andReturnsList() {
        LeaveRequestResponse resp = new LeaveRequestResponse();

        when(queryHandler.getByTenant()).thenReturn(List.of(resp));

        List<LeaveRequestResponse> result = service.getRequestsForCurrentTenant();

        assertThat(result).containsExactly(resp);
        verify(queryHandler).getByTenant();
    }
}
