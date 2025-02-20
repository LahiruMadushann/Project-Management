package com.project_management.dto;

import com.project_management.models.TeamAssignment;
import lombok.Data;

import java.util.List;

@Data
public class CombinedFindTeamResponseDto {

    private List<TeamAssignment> employees;
    private ProjectBudgetGraphDto graph;
}
