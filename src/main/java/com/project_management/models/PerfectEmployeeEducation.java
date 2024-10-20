package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "perfect_employee_educations")
public class PerfectEmployeeEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeEducationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_employee_id", nullable = false)
    private PerfectEmployee employee;

    @Column(name = "p_education_name", nullable = false)
    private String educationName;

    @Column(name = "p_education_points", nullable = false)
    private Integer educationPoints;

    @Column(name = "p_education_weight", nullable = false)
    private Integer educationWeight;
}
