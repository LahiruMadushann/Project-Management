package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employee_educations")
public class EmployeeEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeEducationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "education_name", nullable = false)
    private String educationName;

    @Column(name = "education_points", nullable = false)
    private Integer educationPoints;
}
