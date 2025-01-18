package com.project_management.models;

import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "resources")
public class Resource {
    //Budget Tier,Monthly Cost Range (USD),Service Category,Service Name,Estimated Cost (Monthly)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BudgetTiers tier;
    private Double monthlyCostFloor;
    private Double monthlyCostCeiling;
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;
    private Double EstimatedCostMonthly;
}
