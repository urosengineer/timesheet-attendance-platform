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
public class LeaveRequestSeeder {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final WorkflowLogRepository workflowLogRepository;

    public void seedIfTableEmpty() {
        if (leaveRequestRepository.count() == 0) {
            Optional<User> michaelOpt = userRepository.findByUsername("michael.evans");
            Optional<User> alisonOpt = userRepository.findByUsername("alison.carter");
            Optional<User> liamOpt = userRepository.findByUsername("liam.martin");

            if (michaelOpt.isPresent() && alisonOpt.isPresent()) {
                User michael = michaelOpt.get();
                User alison = alisonOpt.get();
                createLeaveRequestIfNotExists(
                        michael, michael.getOrganization(),
                        LocalDate.now().plusDays(5), LocalDate.now().plusDays(10),
                        "annual", "SUBMITTED", alison, null, "Requesting annual leave for vacation"
                );
            }

            if (alisonOpt.isPresent()) {
                User alison = alisonOpt.get();
                createLeaveRequestIfNotExists(
                        alison, alison.getOrganization(),
                        LocalDate.now().plusDays(2), LocalDate.now().plusDays(3),
                        "sick", "DRAFT", null, null, "Mild symptoms"
                );
            }

            if (liamOpt.isPresent() && alisonOpt.isPresent()) {
                User liam = liamOpt.get();
                User alison = alisonOpt.get();
                createLeaveRequestIfNotExists(
                        liam, liam.getOrganization(),
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(2),
                        "annual", "APPROVED", alison, Instant.now().minus(1, ChronoUnit.DAYS), "Annual leave approved by manager"
                );
            }
        }
    }

    private void createLeaveRequestIfNotExists(
            User user, Organization organization,
            LocalDate startDate, LocalDate endDate, String type, String status,
            User approver, Instant approvedAt, String notes
    ) {
        boolean alreadyExists = leaveRequestRepository.findByUserIdAndStartDateBetween(
                user.getId(), startDate, endDate
        ).stream().anyMatch(lr -> lr.getType().equals(type));
        if (!alreadyExists) {
            LeaveRequest request = LeaveRequest.builder()
                    .user(user)
                    .organization(organization)
                    .startDate(startDate)
                    .endDate(endDate)
                    .type(type)
                    .status(status)
                    .approver(approver)
                    .approvedAt(approvedAt)
                    .notes(notes)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            leaveRequestRepository.save(request);

            createWorkflowLogIfNotExistsForLeave(request, "NONE", "DRAFT", user, "Leave request created");

            if ("SUBMITTED".equals(status)) {
                createWorkflowLogIfNotExistsForLeave(request, "DRAFT", "SUBMITTED", user, "Submitted for approval");
            } else if ("APPROVED".equals(status) && approver != null) {
                createWorkflowLogIfNotExistsForLeave(request, "SUBMITTED", "APPROVED", approver, "Leave approved");
            }
        }
    }

    private void createWorkflowLogIfNotExistsForLeave(
            LeaveRequest request, String oldStatus, String newStatus, User performedBy, String comment
    ) {
        var existing = workflowLogRepository.findByRelatedEntityTypeAndRelatedEntityId(
                "LeaveRequest", request.getId());
        boolean exists = existing.stream()
                .anyMatch(log -> log.getOldStatus().equals(oldStatus) && log.getNewStatus().equals(newStatus));
        if (!exists) {
            WorkflowLog log = WorkflowLog.builder()
                    .relatedEntityType("LeaveRequest")
                    .relatedEntityId(request.getId())
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