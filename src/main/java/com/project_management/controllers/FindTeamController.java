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
    public ResponseEntity<?> getTeam(@RequestBody FindTeamDTO findTeamDTO) {
        try {
            List<TeamAssignment> assignedTeam = teamService.findAndAssignTeam(findTeamDTO);
            return ResponseEntity.ok(assignedTeam);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error finding team: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTeamByProject(@PathVariable Long projectId) {
        try {
            List<TeamAssignment> team = teamService.getTeamByProjectId(projectId);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving team: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTeam(@RequestBody TeamUpdateDTO updateDTO) {
        try {
            List<TeamAssignment> updatedTeam = teamService.updateTeam(updateDTO);
            return ResponseEntity.ok(updatedTeam);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating team: " + e.getMessage());
        }
    }

    @PutMapping("/project/{projectId}/member")
    public ResponseEntity<?> updateTeamMember(
            @PathVariable Long projectId,
            @RequestBody TeamMemberUpdateDTO updateDTO) {
        try {
            TeamAssignment updatedMember = teamService.updateTeamMember(projectId, updateDTO);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating team member: " + e.getMessage());
        }
    }
}