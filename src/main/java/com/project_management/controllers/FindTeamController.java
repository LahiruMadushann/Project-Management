package com.project_management.controllers;

import com.project_management.dto.*;
import com.project_management.models.TeamAssignment;
import com.project_management.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/team")
public class FindTeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping("/find")
    public ResponseEntity<?> getTeam(@RequestBody FindTeamDTO findTeamDTO) {
        try {
            CombinedFindTeamResponseDto assignedTeam = teamService.findAndAssignTeam(findTeamDTO);
            return ResponseEntity.ok(assignedTeam);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTeamByProject(@PathVariable Long projectId) {
        try {
            List<TeamAssignmentDTO> team = teamService.getTeamByProjectId(projectId);
            return ResponseEntity.ok(team);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateTeam(@RequestBody TeamUpdateDTO updateDTO) {
        try {
            List<TeamAssignment> updatedTeam = teamService.updateTeam(updateDTO);
            return ResponseEntity.ok(updatedTeam);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping("/project/{projectId}/member")
    public ResponseEntity<?> updateTeamMember(
            @PathVariable Long projectId,
            @RequestBody TeamMemberUpdateDTO updateDTO) {
        try {
            TeamAssignment updatedMember = teamService.updateTeamMember(projectId, updateDTO);
            return ResponseEntity.ok(updatedMember);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}