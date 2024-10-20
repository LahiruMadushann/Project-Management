package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "perfect_employee_experiences")
public class PerfectEmployeeExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeExperienceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_employee_id", nullable = false)
    private PerfectEmployee employee;

    @Column(name = "p_experience_name", nullable = false)
    private String experienceName;

    @Column(name = "p_experience_points", nullable = false)
    private Integer experiencePoints;

    @Column(name = "p_experience_weight", nullable = false)
    private Integer experienceWeight;
}
