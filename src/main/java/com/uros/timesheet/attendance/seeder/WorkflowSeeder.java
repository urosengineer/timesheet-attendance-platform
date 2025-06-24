package com.uros.timesheet.attendance.seeder;

import com.uros.timesheet.attendance.workflow.WorkflowDefinition;
import com.uros.timesheet.attendance.workflow.WorkflowDefinitionRepository;
import com.uros.timesheet.attendance.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkflowSeeder {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    /**
     * Seeds workflow definitions for demo/test purposes.
     * Safe to call multiple times; does not overwrite if workflow already exists.
     */
    public void seedIfTableEmpty() {
        if (workflowDefinitionRepository.findByEntityType("LeaveRequest").isEmpty()) {
            WorkflowDefinition leaveRequestWorkflow = WorkflowDefinition.builder()
                    .entityType("LeaveRequest")
                    .description("Standard leave request approval workflow")
                    .build();

            WorkflowStep draft = WorkflowStep.builder()
                    .status("DRAFT")
                    .allowedTransitions(Set.of("SUBMITTED"))
                    .allowedRoles(Set.of("EMPLOYEE"))
                    .build();

            WorkflowStep submitted = WorkflowStep.builder()
                    .status("SUBMITTED")
                    .allowedTransitions(Set.of("APPROVED", "REJECTED"))
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            WorkflowStep approved = WorkflowStep.builder()
                    .status("APPROVED")
                    .allowedTransitions(Set.of())
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            WorkflowStep rejected = WorkflowStep.builder()
                    .status("REJECTED")
                    .allowedTransitions(Set.of())
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            leaveRequestWorkflow.addStep(draft);
            leaveRequestWorkflow.addStep(submitted);
            leaveRequestWorkflow.addStep(approved);
            leaveRequestWorkflow.addStep(rejected);

            workflowDefinitionRepository.save(leaveRequestWorkflow);
        }

        if (workflowDefinitionRepository.findByEntityType("AttendanceRecord").isEmpty()) {
            WorkflowDefinition attendanceWorkflow = WorkflowDefinition.builder()
                    .entityType("AttendanceRecord")
                    .description("Attendance record approval workflow")
                    .build();

            WorkflowStep draft = WorkflowStep.builder()
                    .status("DRAFT")
                    .allowedTransitions(Set.of("SUBMITTED"))
                    .allowedRoles(Set.of("EMPLOYEE"))
                    .build();

            WorkflowStep submitted = WorkflowStep.builder()
                    .status("SUBMITTED")
                    .allowedTransitions(Set.of("APPROVED", "REJECTED"))
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            WorkflowStep approved = WorkflowStep.builder()
                    .status("APPROVED")
                    .allowedTransitions(Set.of())
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            WorkflowStep rejected = WorkflowStep.builder()
                    .status("REJECTED")
                    .allowedTransitions(Set.of()) // terminal state
                    .allowedRoles(Set.of("MANAGER", "ADMIN"))
                    .build();

            attendanceWorkflow.addStep(draft);
            attendanceWorkflow.addStep(submitted);
            attendanceWorkflow.addStep(approved);
            attendanceWorkflow.addStep(rejected);

            workflowDefinitionRepository.save(attendanceWorkflow);
        }
    }
}