package com.project_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BudgetMLResponseDto {

    @JsonProperty("estimated_budget")
    private Double budget;
}
