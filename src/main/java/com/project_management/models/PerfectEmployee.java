package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "perfect_employees")
public class PerfectEmployee {
    @Id
    @Column(name = "p_employee_id")
    private String employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "p_created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "p_role_name", nullable = false)
    private String roleName;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeSkill> skills;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeExperience> experiences;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeEducation> educations;


}
