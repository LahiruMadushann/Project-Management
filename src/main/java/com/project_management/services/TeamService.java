package com.project_management.services;

import com.project_management.dto.CombinedFindTeamResponseDto;
import com.project_management.dto.FindTeamDTO;
import com.project_management.dto.TeamMemberUpdateDTO;
import com.project_management.dto.TeamUpdateDTO;
import com.project_management.models.TeamAssignment;

import java.util.List;

public interface TeamService {
    CombinedFindTeamResponseDto findAndAssignTeam(FindTeamDTO findTeamDTO);
    List<TeamAssignment> updateTeam(TeamUpdateDTO updateDTO);
    TeamAssignment updateTeamMember(Long projectId, TeamMemberUpdateDTO updateDTO);
    List<TeamAssignment> getTeamByProjectId(Long projectId);
}
