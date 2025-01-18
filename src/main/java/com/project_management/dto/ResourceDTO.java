package com.project_management.dto;

import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class ResourceDTO {

    private long id;
    private String tier;
    private Double monthlyCostFloor;
    private Double monthlyCostCeiling;
    private String resourceType;
    private Double EstimatedCostMonthly;
}
