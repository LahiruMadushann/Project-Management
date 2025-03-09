package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_assignments")
@Data
public class TeamAssignment {
    @EmbeddedId
    private TeamAssignmentId id;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name="kpi")
    private Double kpi;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
