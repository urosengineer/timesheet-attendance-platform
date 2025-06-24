package com.uros.timesheet.attendance.workflow;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_definitions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WorkflowDefinition {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String entityType;

    @OneToMany(
            mappedBy = "workflowDefinition",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();

    private String description;

    public void addStep(WorkflowStep step) {
        steps.add(step);
        step.setWorkflowDefinition(this);
    }

    public void removeStep(WorkflowStep step) {
        steps.remove(step);
        step.setWorkflowDefinition(null);
    }
}