package com.project_management.dto;

import lombok.Data;

@Data
public class ResourceCatalogDto {
    private Long projectId;
    private Integer serviceYears;
    private Double budget;
    private Double cloudPercentage;
    private Double dbPercentage;
    private Double idePercentage;
    private Double automationPercentage;
    private Double securityPercentage;
    private Double collaborationPercentage;
}
