package com.project_management.controllers;

import com.project_management.dto.FindTeamDTO;
import com.project_management.dto.TeamMemberUpdateDTO;
import com.project_management.dto.TeamUpdateDTO;
import com.project_management.models.TeamAssignment;
import com.project_management.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class FindTeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping("/find")
    public ResponseEntity<List<TeamAssignment>> getTeam(@RequestBody FindTeamDTO findTeamDTO) {
        List<TeamAssignment> assignedTeam = teamService.findAndAssignTeam(findTeamDTO);
        return ResponseEntity.ok(assignedTeam);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TeamAssignment>> getTeamByProject(@PathVariable Long projectId) {
        List<TeamAssignment> team = teamService.getTeamByProjectId(projectId);
        return ResponseEntity.ok(team);
    }

    @PutMapping("/update")
    public ResponseEntity<List<TeamAssignment>> updateTeam(@RequestBody TeamUpdateDTO updateDTO) {
        List<TeamAssignment> updatedTeam = teamService.updateTeam(updateDTO);
        return ResponseEntity.ok(updatedTeam);
    }

    @PutMapping("/project/{projectId}/member")
    public ResponseEntity<TeamAssignment> updateTeamMember(
            @PathVariable Long projectId,
            @RequestBody TeamMemberUpdateDTO updateDTO) {
        TeamAssignment updatedMember = teamService.updateTeamMember(projectId, updateDTO);
        return ResponseEntity.ok(updatedMember);
    }
}
