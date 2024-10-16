package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee_experiences")
public class EmployeeExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeExperienceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "experience_name", nullable = false)
    private String experienceName;

    @Column(name = "experience_points", nullable = false)
    private Integer experiencePoints;
}

