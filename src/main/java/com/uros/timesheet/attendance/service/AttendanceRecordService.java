package com.uros.timesheet.attendance.service;

import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordCreateRequest;
import com.uros.timesheet.attendance.dto.attendance.AttendanceRecordResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceRecordService {
    AttendanceRecordResponse createRecord(AttendanceRecordCreateRequest request);
    AttendanceRecordResponse submitRecord(UUID id, UUID currentUserId);
    AttendanceRecordResponse approveRecord(UUID id, UUID approverId);
    AttendanceRecordResponse rejectRecord(UUID id, UUID approverId, String reason);
    AttendanceRecordResponse getRecordById(UUID id);
    List<AttendanceRecordResponse> getRecordsForUser(UUID userId);

    AttendanceRecordResponse softDeleteRecord(UUID id, UUID performedByUserId, String reason);
    AttendanceRecordResponse restoreRecord(UUID id, UUID performedByUserId, String reason);

    List<AttendanceRecordResponse> getRecordsForCurrentTenant();
}