package com.project_management.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RoleDistribution {
    private Map<String, Map<String, Double>> distributions = new HashMap<>();
}
