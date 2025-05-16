package com.project_management.models;

import com.project_management.models.enums.Domain;
import com.project_management.models.enums.RoleCategory;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Random;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "e_role_category", nullable = true)
    private RoleCategory roleCategory;

    @Column(name = "seniority", nullable = false)
    private String seniority;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeSkill> skills;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeExperience> experiences;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeEducation> educations;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associated_user_id")
    private User user;

    @Column(name = "maximum_assessed_count", nullable = false)
    private Integer maximumAssessedCount;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", nullable = false)
    private Domain domain;


    @Column(name = "initial_kpi", nullable = false)
    private Double kpi;

    private Double salary;

    public void setKpi(double kpi){
        if(kpi> 100){
            Random random = new Random();
            double upperLimit = 80 + (random.nextDouble() * 20);
            this.kpi = Math.min(kpi, upperLimit);
        }else{
            this.kpi =kpi;
        }
    }
}

