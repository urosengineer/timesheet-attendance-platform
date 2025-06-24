package com.uros.timesheet.attendance.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<User> users;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted() {
        this.deletedAt = Instant.now();
        this.status = "DELETED";
    }

    public void restore() {
        this.deletedAt = null;
        if (!"ACTIVE".equals(this.status)) {
            this.status = "ACTIVE";
        }
    }
}