package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.repositories.*;
import com.project_management.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private KpiService kpiService;

    @Autowired
    private TeamAssignmentRepository teamAssignmentRepository;

    @Override
    @Transactional
    public List<TeamAssignment> findAndAssignTeam(FindTeamDTO findTeamDTO) {
        // Get all employees and their KPIs
        List<EmployeeDTO> baseEmployees = employeeService.getAllEmployees();
        List<KpiDTO> employeeKpis = kpiService.calculateKpiForAllEmployees();

        // Create a map of KPIs for quick lookup
        Map<String, KpiDTO> kpiMap = employeeKpis.stream()
                .collect(Collectors.toMap(KpiDTO::getEmployeeId, kpi -> kpi));

        // Filter employees based on required skills and maximum assessment count
        List<EmployeeDTO> filteredEmployees = baseEmployees.stream()
                .filter(employee -> hasRequiredSkills(employee, findTeamDTO.getRequiredSkills()))
                .filter(employee -> isWithinAssessmentLimit(employee))
                .collect(Collectors.toList());

        // Group employees by role
        Map<String, List<EmployeeDTO>> employeesByRole = filteredEmployees.stream()
                .collect(Collectors.groupingBy(EmployeeDTO::getRoleName));

        // Select team members based on required roles and KPI scores
        List<TeamAssignment> selectedTeam = new ArrayList<>();

        for (TeamRolesDTO roleReq : findTeamDTO.getRequiredRoles()) {
            List<EmployeeDTO> roleEmployees = employeesByRole.getOrDefault(roleReq.getRoleName(), new ArrayList<>());

            // Sort employees by KPI score
            roleEmployees.sort((e1, e2) -> Double.compare(
                    kpiMap.get(e2.getEmployeeId()).getOverallKpi(),
                    kpiMap.get(e1.getEmployeeId()).getOverallKpi()
            ));

            // Select top N employees for this role
            int selectedCount = 0;
            int index = 0;

            while (selectedCount < roleReq.getRequiredNumber() && index < roleEmployees.size()) {
                EmployeeDTO selectedEmployee = roleEmployees.get(index);

                // Double-check assessment limit before final selection
                if (isWithinAssessmentLimit(selectedEmployee)) {
                    TeamAssignment assignment = createTeamAssignment(findTeamDTO.getProjectId(), selectedEmployee);
                    selectedTeam.add(assignment);
                    selectedCount++;
                }
                index++;
            }

            // If couldn't find enough employees for this role, log a warning
            if (selectedCount < roleReq.getRequiredNumber()) {
                String warningMessage = String.format(
                        "Could not find enough eligible employees for role %s. Required: %d, Found: %d",
                        roleReq.getRoleName(),
                        roleReq.getRequiredNumber(),
                        selectedCount
                );
                log.info(warningMessage);
            }
        }

        // Save all team assignments
        return teamAssignmentRepository.saveAll(selectedTeam);
    }

    private boolean hasRequiredSkills(EmployeeDTO employee, List<TeamSkillsDTO> requiredSkills) {
        Set<String> employeeSkills = employee.getSkills().stream()
                .map(EmployeeSkillDTO::getSkillName)
                .collect(Collectors.toSet());

        return requiredSkills.stream()
                .allMatch(required -> employeeSkills.contains(required.getSkillName()));
    }

    private boolean isWithinAssessmentLimit(EmployeeDTO employee) {
        int currentAssignments = teamAssignmentRepository.countAssignmentsByEmployeeId(employee.getEmployeeId());
        return currentAssignments < employee.getMaximumAssessedCount();
    }

    private TeamAssignment createTeamAssignment(Long projectId, EmployeeDTO employee) {
        TeamAssignment assignment = new TeamAssignment();

        TeamAssignmentId id = new TeamAssignmentId();
        id.setProjectId(projectId);
        id.setEmployeeId(employee.getEmployeeId());

        assignment.setId(id);
        assignment.setEmployeeName(employee.getEmployeeName());
        assignment.setRoleName(employee.getRoleName());

        return assignment;
    }

    @Override
    @Transactional
    public List<TeamAssignment> updateTeam(TeamUpdateDTO updateDTO) {
        // Validate project exists
        List<TeamAssignment> currentTeam = teamAssignmentRepository.findByIdProjectId(updateDTO.getProjectId());
        if (currentTeam.isEmpty()) {
            throw new RuntimeException("Project team not found");
        }

        List<TeamAssignment> updatedTeam = new ArrayList<>();

        for (TeamMemberUpdateDTO memberUpdate : updateDTO.getTeamMembers()) {
            // Handle the update for each team member
            try {
                TeamAssignment updatedMember = updateTeamMember(updateDTO.getProjectId(), memberUpdate);
                updatedTeam.add(updatedMember);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update team member: " + e.getMessage());
            }
        }

        return updatedTeam;
    }

    @Override
    @Transactional
    public TeamAssignment updateTeamMember(Long projectId, TeamMemberUpdateDTO updateDTO) {
        // If there's a current employee, remove them from the team
        if (updateDTO.getCurrentEmployeeId() != null) {
            teamAssignmentRepository.deleteByIdProjectIdAndIdEmployeeId(
                    projectId,
                    updateDTO.getCurrentEmployeeId()
            );
        }

        // Validate new employee
        EmployeeDTO newEmployee = employeeService.getEmployee(updateDTO.getNewEmployeeId());
        if (newEmployee == null) {
            throw new RuntimeException("New employee not found");
        }

        // Check if new employee is within assessment limit
        if (!isWithinAssessmentLimit(newEmployee)) {
            throw new RuntimeException("New employee has reached maximum assessment count");
        }

        // Create and save new assignment
        TeamAssignment newAssignment = new TeamAssignment();
        TeamAssignmentId id = new TeamAssignmentId();
        id.setProjectId(projectId);
        id.setEmployeeId(newEmployee.getEmployeeId());

        newAssignment.setId(id);
        newAssignment.setEmployeeName(newEmployee.getEmployeeName());
        newAssignment.setRoleName(updateDTO.getRoleName());

        return teamAssignmentRepository.save(newAssignment);
    }

    @Override
    public List<TeamAssignment> getTeamByProjectId(Long projectId) {
        return teamAssignmentRepository.findByIdProjectId(projectId);
    }
}

