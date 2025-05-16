package com.project_management.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectBudgetGraphDto {

    private Long projectId;
    private long fullBudget;
    private double fullWeight;
    private double salary;
    private double salaryWeigh;
    private double resources;
    private double resourcesWeight;
    private double otherExpenses;
    private double otherExpensesWight;
    private double profit;
    private double profitWeight;
    private double unassigned;
    private double unassginedWeight;


    private BigDecimal expectedBudget;
    private String budgetRisk;
    private String predictedDate;
    private String dateRisk;
}
