package com.uros.timesheet.attendance.workflow;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "workflow_steps")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WorkflowStep {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition workflowDefinition;

    @Column(nullable = false)
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workflow_step_allowed_transitions",
            joinColumns = @JoinColumn(name = "workflow_step_id")
    )
    @Column(name = "allowed_status")
    @Builder.Default
    private Set<String> allowedTransitions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "workflow_step_allowed_roles",
            joinColumns = @JoinColumn(name = "workflow_step_id")
    )
    @Column(name = "role")
    @Builder.Default
    private Set<String> allowedRoles = new HashSet<>();

    private String conditionExpression;
}