package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.domain.AttendanceRecord;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordCreateRequest;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.mapper.AttendanceRecordMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.service.handler.*;
import com.uros.timesheet.attendance.util.TenantContext;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {AttendanceRecordServiceImpl.class})
class AttendanceRecordServiceImplTest {

    @MockBean private AttendanceRecordCreateHandler createHandler;
    @MockBean private AttendanceRecordSubmitHandler submitHandler;
    @MockBean private AttendanceRecordApproveHandler approveHandler;
    @MockBean private AttendanceRecordRejectHandler rejectHandler;
    @MockBean private AttendanceRecordDeleteHandler deleteHandler;
    @MockBean private AttendanceRecordRestoreHandler restoreHandler;
    @MockBean private AttendanceRecordRepository attendanceRecordRepository;
    @MockBean private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private AttendanceRecordServiceImpl service;

    @MockBean
    private MessageUtil messageUtil;

    // --- CREATE ---
    @Test
    void createRecord_delegatesToHandler_andReturnsResponse() {
        AttendanceRecordCreateRequest req = new AttendanceRecordCreateRequest();
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(createHandler.handle(req)).thenReturn(expected);

        AttendanceRecordResponse result = service.createRecord(req);

        assertThat(result).isSameAs(expected);
        verify(createHandler).handle(req);
    }

    // --- SUBMIT ---
    @Test
    void submitRecord_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(submitHandler.handle(id, userId)).thenReturn(expected);

        AttendanceRecordResponse result = service.submitRecord(id, userId);

        assertThat(result).isSameAs(expected);
        verify(submitHandler).handle(id, userId);
    }

    // --- APPROVE ---
    @Test
    void approveRecord_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(approveHandler.handle(id, approverId)).thenReturn(expected);

        AttendanceRecordResponse result = service.approveRecord(id, approverId);

        assertThat(result).isSameAs(expected);
        verify(approveHandler).handle(id, approverId);
    }

    // --- REJECT ---
    @Test
    void rejectRecord_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();
        String reason = "not valid";
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(rejectHandler.handle(id, approverId, reason)).thenReturn(expected);

        AttendanceRecordResponse result = service.rejectRecord(id, approverId, reason);

        assertThat(result).isSameAs(expected);
        verify(rejectHandler).handle(id, approverId, reason);
    }

    // --- SOFT DELETE ---
    @Test
    void softDeleteRecord_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String reason = "Test";
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(deleteHandler.handle(id, userId, reason)).thenReturn(expected);

        AttendanceRecordResponse result = service.softDeleteRecord(id, userId, reason);

        assertThat(result).isSameAs(expected);
        verify(deleteHandler).handle(id, userId, reason);
    }

    // --- RESTORE ---
    @Test
    void restoreRecord_delegatesToHandler_andReturnsResponse() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String reason = "Test";
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(restoreHandler.handle(id, userId, reason)).thenReturn(expected);

        AttendanceRecordResponse result = service.restoreRecord(id, userId, reason);

        assertThat(result).isSameAs(expected);
        verify(restoreHandler).handle(id, userId, reason);
    }

    // --- GET BY ID ---
    @Test
    void getRecordById_returnsMappedResponse_whenFound() {
        UUID id = UUID.randomUUID();
        AttendanceRecord entity = AttendanceRecord.builder().id(id).build();
        AttendanceRecordResponse expected = new AttendanceRecordResponse();

        when(attendanceRecordRepository.findById(id)).thenReturn(Optional.of(entity));
        when(attendanceRecordMapper.toResponse(entity)).thenReturn(expected);

        AttendanceRecordResponse result = service.getRecordById(id);

        assertThat(result).isSameAs(expected);
        verify(attendanceRecordRepository).findById(id);
        verify(attendanceRecordMapper).toResponse(entity);
    }

    @Test
    void getRecordById_throwsNotFoundException_whenAbsent() {
        UUID id = UUID.randomUUID();
        when(attendanceRecordRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRecordById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("error.attendance.not.found");
    }

    // --- GET FOR USER ---
    @Test
    void getRecordsForUser_mapsEntitiesToResponses() {
        UUID userId = UUID.randomUUID();
        AttendanceRecord entity = AttendanceRecord.builder().id(UUID.randomUUID()).build();
        AttendanceRecordResponse resp = new AttendanceRecordResponse();

        when(attendanceRecordRepository.findByUserId(userId)).thenReturn(List.of(entity));
        when(attendanceRecordMapper.toResponse(entity)).thenReturn(resp);

        List<AttendanceRecordResponse> result = service.getRecordsForUser(userId);

        assertThat(result).containsExactly(resp);
        verify(attendanceRecordRepository).findByUserId(userId);
    }

    // --- GET FOR TENANT ---
    @Test
    void getRecordsForCurrentTenant_mapsEntitiesToResponses() {
        UUID tenantId = UUID.randomUUID();
        AttendanceRecord entity = AttendanceRecord.builder().id(UUID.randomUUID()).build();
        AttendanceRecordResponse resp = new AttendanceRecordResponse();

        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getTenantId).thenReturn(tenantId.toString());

            when(attendanceRecordRepository.findByOrganizationId(tenantId)).thenReturn(List.of(entity));
            when(attendanceRecordMapper.toResponse(entity)).thenReturn(resp);

            List<AttendanceRecordResponse> result = service.getRecordsForCurrentTenant();

            assertThat(result).containsExactly(resp);
            verify(attendanceRecordRepository).findByOrganizationId(tenantId);
        }
    }

    @BeforeEach
    void setup() {
        when(messageUtil.get("error.tenant.not.set")).thenReturn("error.tenant.not.set");
    }

    @Test
    void getRecordsForCurrentTenant_throws_whenNoTenantContext() {
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getTenantId).thenReturn(null);

            assertThatThrownBy(() -> service.getRecordsForCurrentTenant())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("error.tenant.not.set");
        }
    }
}