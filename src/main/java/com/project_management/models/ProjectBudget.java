package com.project_management.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "project_budget")
public class ProjectBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    private Double employeeSalaryWeight;
    private Double resourceWeight;
    private Double profitWeight;
    private Double misWeight;

    private Double predictedBudget;
    private Double estimatedBudget;

    private Boolean BudgetApproved = true;
}
