package com.project_management.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class TeamAssignmentId implements Serializable {
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "employee_id")
    private String employeeId;
}
