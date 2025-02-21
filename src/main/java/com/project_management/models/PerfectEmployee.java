package com.project_management.models;

import com.project_management.models.enums.RoleCategory;
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

    @Column(name = "p_role_short_name", nullable = false)
    private String roleShortName;

    @Column(name = "p_role_distribution_value", nullable = false)
    private Integer roleDistributionValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "p_role_category", nullable = true)
    private RoleCategory roleCategory;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeSkill> skills;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeExperience> experiences;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerfectEmployeeEducation> educations;


}
