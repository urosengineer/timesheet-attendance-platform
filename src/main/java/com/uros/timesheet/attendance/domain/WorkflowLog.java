package com.uros.timesheet.attendance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_logs")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class WorkflowLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String relatedEntityType; // npr. "AttendanceRecord"

    @Column(nullable = false)
    private UUID relatedEntityId;

    @Column(nullable = false)
    private String oldStatus;

    @Column(nullable = false)
    private String newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private com.uros.timesheet.attendance.domain.User user;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    private String comment;
}