package com.project_management.services;

import com.project_management.dto.*;
import com.project_management.models.TeamAssignment;

import java.util.List;

public interface TeamService {
    CombinedFindTeamResponseDto findAndAssignTeam(FindTeamDTO findTeamDTO);
    List<TeamAssignment> updateTeam(TeamUpdateDTO updateDTO);
    TeamAssignment updateTeamMember(Long projectId, TeamMemberUpdateDTO updateDTO);
    List<TeamAssignmentDTO> getTeamByProjectId(Long projectId);
}
