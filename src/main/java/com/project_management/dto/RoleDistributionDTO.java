package com.project_management.dto;

import java.util.Map;

public class RoleDistributionDTO {
    private Map<String, Map<String, Double>> roleDistribution;
    private Map<String, Integer> maxStoryPoints;

    public Map<String, Map<String, Double>> getRoleDistribution() {
        return roleDistribution;
    }

    public Map<String, Integer> getMaxStoryPoints() {
        return maxStoryPoints;
    }
}
