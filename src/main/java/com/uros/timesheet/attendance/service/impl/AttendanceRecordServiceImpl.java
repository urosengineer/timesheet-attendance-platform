package com.uros.timesheet.attendance.service.impl;

import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordCreateRequest;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;
import com.uros.timesheet.attendance.exception.NotFoundException;
import com.uros.timesheet.attendance.mapper.AttendanceRecordMapper;
import com.uros.timesheet.attendance.repository.AttendanceRecordRepository;
import com.uros.timesheet.attendance.service.AttendanceRecordService;
import com.uros.timesheet.attendance.service.handler.*;
import com.uros.timesheet.attendance.util.TenantContext;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private final AttendanceRecordCreateHandler createHandler;
    private final AttendanceRecordSubmitHandler submitHandler;
    private final AttendanceRecordApproveHandler approveHandler;
    private final AttendanceRecordRejectHandler rejectHandler;
    private final AttendanceRecordDeleteHandler deleteHandler;
    private final AttendanceRecordRestoreHandler restoreHandler;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final MessageUtil messageUtil;

    @Override
    @Transactional
    public AttendanceRecordResponse createRecord(AttendanceRecordCreateRequest request) {
        return createHandler.handle(request);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse submitRecord(UUID id, UUID currentUserId) {
        return submitHandler.handle(id, currentUserId);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse approveRecord(UUID id, UUID approverId) {
        return approveHandler.handle(id, approverId);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse rejectRecord(UUID id, UUID approverId, String reason) {
        return rejectHandler.handle(id, approverId, reason);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse softDeleteRecord(UUID id, UUID performedByUserId, String reason) {
        return deleteHandler.handle(id, performedByUserId, reason);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse restoreRecord(UUID id, UUID performedByUserId, String reason) {
        return restoreHandler.handle(id, performedByUserId, reason);
    }

    @Override
    public AttendanceRecordResponse getRecordById(UUID id) {
        return attendanceRecordRepository.findById(id)
                .map(attendanceRecordMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("error.attendance.not.found"));
    }

    @Override
    public List<AttendanceRecordResponse> getRecordsForUser(UUID userId) {
        return attendanceRecordRepository.findByUserId(userId).stream()
                .map(attendanceRecordMapper::toResponse)
                .toList();
    }

    public List<AttendanceRecordResponse> getRecordsForCurrentTenant() {
        String tenantIdString = TenantContext.getTenantId();
        if (tenantIdString == null) {
            throw new IllegalStateException(messageUtil.get("error.tenant.not.set"));
        }
        UUID tenantId = UUID.fromString(tenantIdString);
        return attendanceRecordRepository.findByOrganizationId(tenantId).stream()
                .map(attendanceRecordMapper::toResponse)
                .toList();
    }
}