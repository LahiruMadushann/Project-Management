package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "perfect_employee_skills")
public class PerfectEmployeeSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_employee_id", nullable = false)
    private Employee employee;

    @Column(name = "p_skill_name", nullable = false)
    private String skillName;

    @Column(name = "p_skill_points", nullable = false)
    private Integer skillPoints;

    @Column(name = "p_skill_weight", nullable = false)
    private Integer skillWeight;
}
