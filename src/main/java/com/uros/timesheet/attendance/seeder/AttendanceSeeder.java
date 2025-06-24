package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.domain.*;
import com.uros.timesheet.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttendanceSeeder {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final WorkflowLogRepository workflowLogRepository;

    public void seedIfTableEmpty() {
        if (attendanceRecordRepository.count() == 0) {
            Optional<User> urosOpt = userRepository.findByUsername("uros");
            Optional<User> michaelOpt = userRepository.findByUsername("michael.evans");
            Optional<User> alisonOpt = userRepository.findByUsername("alison.carter");
            Optional<User> danielOpt = userRepository.findByUsername("daniel.moore");
            Optional<User> liamOpt = userRepository.findByUsername("liam.martin");
            Optional<User> oliviaOpt = userRepository.findByUsername("olivia.jones");

            Organization cloudCore = organizationRepository.findByName("CloudCore").orElseThrow();
            Organization acmeLtd = organizationRepository.findByName("Acme Ltd").orElseThrow();
            Organization globalTech = organizationRepository.findByName("GlobalTech").orElseThrow();

            if (urosOpt.isPresent() && alisonOpt.isPresent()) {
                User uros = urosOpt.get();
                User alison = alisonOpt.get();
                createAttendanceRecordIfNotExists(
                        uros, cloudCore, LocalDate.now().minusDays(2), LocalTime.of(9, 0), LocalTime.of(17, 0),
                        "work", "APPROVED", alison, Instant.now().minus(1, ChronoUnit.DAYS), "Regular work day - approved by manager"
                );
                createAttendanceRecordIfNotExists(
                        uros, cloudCore, LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(14, 0),
                        "work", "DRAFT", null, null, "Left early (draft)"
                );
            }

            if (michaelOpt.isPresent() && alisonOpt.isPresent()) {
                User michael = michaelOpt.get();
                User alison = alisonOpt.get();
                createAttendanceRecordIfNotExists(
                        michael, cloudCore, LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(18, 0),
                        "work", "SUBMITTED", alison, null, "Late arrival - submitted for approval"
                );
            }

            if (danielOpt.isPresent() && oliviaOpt.isPresent()) {
                User daniel = danielOpt.get();
                User olivia = oliviaOpt.get();
                createAttendanceRecordIfNotExists(
                        daniel, acmeLtd, LocalDate.now().minusDays(3), LocalTime.of(8, 30), LocalTime.of(16, 30),
                        "remote", "APPROVED", olivia, Instant.now().minus(2, ChronoUnit.DAYS), "Remote work - approved"
                );
            }

            if (liamOpt.isPresent()) {
                User liam = liamOpt.get();
                createAttendanceRecordIfNotExists(
                        liam, globalTech, LocalDate.now().minusDays(4), LocalTime.of(9, 0), LocalTime.of(17, 0),
                        "sick", "SUBMITTED", null, null, "Sick day submitted"
                );
            }
        }
    }

    private void createAttendanceRecordIfNotExists(
            User user, Organization organization, LocalDate date,
            LocalTime startTime, LocalTime endTime, String type, String status,
            User approver, Instant approvedAt, String notes
    ) {
        boolean alreadyExists = attendanceRecordRepository.findByUserIdAndDateBetween(user.getId(), date, date)
                .stream().anyMatch(ar -> ar.getType().equals(type));
        if (!alreadyExists) {
            AttendanceRecord record = AttendanceRecord.builder()
                    .user(user)
                    .organization(organization)
                    .date(date)
                    .startTime(startTime)
                    .endTime(endTime)
                    .type(type)
                    .status(status)
                    .approver(approver)
                    .approvedAt(approvedAt)
                    .notes(notes)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            attendanceRecordRepository.save(record);

            createWorkflowLogIfNotExists(record, "NONE", "DRAFT", user, "Record created");

            if ("SUBMITTED".equals(status)) {
                createWorkflowLogIfNotExists(record, "DRAFT", "SUBMITTED", user, "Submitted for approval");
            } else if ("APPROVED".equals(status) && approver != null) {
                createWorkflowLogIfNotExists(record, "SUBMITTED", "APPROVED", approver, "Attendance approved");
            }
        }
    }

    private void createWorkflowLogIfNotExists(
            AttendanceRecord record, String oldStatus, String newStatus, User performedBy, String comment
    ) {
        var existing = workflowLogRepository.findByRelatedEntityTypeAndRelatedEntityId(
                "AttendanceRecord", record.getId());
        boolean exists = existing.stream()
                .anyMatch(log -> log.getOldStatus().equals(oldStatus) && log.getNewStatus().equals(newStatus));
        if (!exists) {
            WorkflowLog log = WorkflowLog.builder()
                    .relatedEntityType("AttendanceRecord")
                    .relatedEntityId(record.getId())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .user(performedBy)
                    .timestamp(Instant.now())
                    .comment(comment)
                    .build();
            workflowLogRepository.save(log);
        }
    }
}