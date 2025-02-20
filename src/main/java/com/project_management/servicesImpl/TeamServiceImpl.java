package com.project_management.servicesImpl;

import com.project_management.dto.*;
import com.project_management.models.*;
import com.project_management.models.enums.RoleCategory;
import com.project_management.repositories.*;
import com.project_management.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.project_management.models.enums.RoleCategory.*;

@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private KpiService kpiService;

    @Autowired
    private TeamAssignmentRepository teamAssignmentRepository;

    @Autowired
    private ProjectResourceConfigRepository projectResourceConfigRepository;

    @Autowired
    private AdvanceDetailsRepository advanceDetailsRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url.budget}")
    private String budgetUrl;

    @Override
    @Transactional
    public CombinedFindTeamResponseDto findAndAssignTeam(FindTeamDTO findTeamDTO) {
        int devCount = 0;
        int baCount = 0;
        int qaCount = 0;
        int pmCount = 0;
        int devopsCount = 0;

        double fullBudget = 0;
        double fullWeight = 100;
        double salary= 0;
        double salaryWeight = 0;
        double profit= 0;
        double profitWeight = 0;
        double mis= 0;
        double misWeight = 0;
        double resources= 0;
        double resourcesWeight = 0;
        double unassigned= 0;
        double unassginedWeight = 0;
        int duration = 2;


        AdvanceDetails advanceDetails = advanceDetailsRepository.findByProjectId(findTeamDTO.getProjectId());
        ProjectDTO project = projectService.getProjectById(findTeamDTO.getProjectId());

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
                    if(selectedEmployee.getRoleCategory() == RoleCategory.dev || selectedEmployee.getRoleCategory() == RoleCategory.ui){
                        devCount++;
                    }else if(selectedEmployee.getRoleCategory() == qa){
                        qaCount++;
                    }else if(selectedEmployee.getRoleCategory() == pm){
                        pmCount++;
                    }else if(selectedEmployee.getRoleCategory() == devops){
                        devopsCount++;
                    }else{
                        baCount++;
                    }
                    double employeeSalary;
                    if(selectedEmployee.getSalary() != null && selectedEmployee.getSalary() != 0 ){
                        employeeSalary = selectedEmployee.getSalary()* 12 * duration;
                    }else{
                        employeeSalary = 100000 * 12 * duration;
                    }
                    salary = salary + employeeSalary;
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

        List<ProjectResourceConfig> configs = projectResourceConfigRepository.findAllByProjectId(findTeamDTO.getProjectId());
        List<ProjectResourceConfig> reversedList = configs.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), l -> {
                    Collections.reverse(l);
                    return l;
                }));
        ProjectResourceConfig latestConfig = reversedList.get(0);

        BudgetMLRequestDto requestDto = new BudgetMLRequestDto();
        requestDto.setDomain(advanceDetails.getDomain());
        requestDto.setMethodology(advanceDetails.getMethodology()==2?"Agile":"Waterfall");
        requestDto.setDevCount(devCount);
        requestDto.setQaCount(qaCount);
        requestDto.setPmCount(pmCount);
        requestDto.setBaCount(baCount);
        requestDto.setDevOpsCount(devopsCount);
        requestDto.setMiscellaneousExpenses(advanceDetails.getOtherExpenses());
        requestDto.setProfitMargin(advanceDetails.getExpectedProfit());

        // Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BudgetMLRequestDto> entity = new HttpEntity<>(requestDto, headers);

        // Send the request to the ML service
        ResponseEntity<BudgetMLResponseDto> mlResponse = restTemplate.exchange(
                budgetUrl,
                HttpMethod.POST,
                entity,
                BudgetMLResponseDto.class
        );

        // Check the response status and handle errors
        if (!mlResponse.getStatusCode().is2xxSuccessful() || mlResponse.getBody() == null) {
            throw new RuntimeException("Failed to get prediction from ML service: " +
                    mlResponse.getStatusCode());
        }

        fullBudget = mlResponse.getBody().getBudget()*10;

        salaryWeight =(salary/fullBudget)  * 100;
        profitWeight = advanceDetails.getExpectedProfit();
        profit = (fullBudget/100) * profitWeight;
        misWeight = advanceDetails.getOtherExpenses();
        mis = (fullBudget/100) * misWeight;
        resourcesWeight = fullWeight - ( profitWeight + misWeight + salaryWeight);
        resources = (fullBudget/100) * resourcesWeight;

        // Create a formatter for 2 decimal places
        DecimalFormat df = new DecimalFormat("#.##");

// Format all decimal values before setting them
        ProjectBudgetGraphDto graphDto = new ProjectBudgetGraphDto();
        graphDto.setProjectId(project.getId());
        graphDto.setFullBudget((long) Double.parseDouble(df.format(fullBudget)));
        graphDto.setFullWeight(Double.parseDouble(df.format(fullWeight)));
        graphDto.setExpectedBudget(BigDecimal.valueOf(project.getBudget() != null ?
                Double.parseDouble(df.format(project.getBudget())) : null));
        graphDto.setProfit(Double.parseDouble(df.format(profit)));
        graphDto.setProfitWeight(Double.parseDouble(df.format(profitWeight)));
        graphDto.setSalary(Double.parseDouble(df.format(salary)));
        graphDto.setSalaryWeigh(Double.parseDouble(df.format(salaryWeight)));
        graphDto.setOtherExpenses(Double.parseDouble(df.format(mis)));
        graphDto.setOtherExpensesWight(Double.parseDouble(df.format(misWeight)));
        graphDto.setResources(Double.parseDouble(df.format(resources)));
        graphDto.setResourcesWeight(Double.parseDouble(df.format(resourcesWeight)));
        graphDto.setUnassigned(Double.parseDouble(df.format(unassigned)));
        graphDto.setUnassginedWeight(Double.parseDouble(df.format(unassginedWeight)));

        graphDto.setExpectedBudget(project.getPredictedBudgetForResources());
        graphDto.setBudgetRisk(
                BigDecimal.valueOf(fullBudget).compareTo(project.getBudget()) < 0 ? "LOW" : "HIGH"
        );



        CombinedFindTeamResponseDto combinedFindTeamResponseDto = new CombinedFindTeamResponseDto();
        combinedFindTeamResponseDto.setEmployees(teamAssignmentRepository.saveAll(selectedTeam));
        combinedFindTeamResponseDto.setGraph(graphDto);

        project.setPredictedBudgetForResources(BigDecimal.valueOf(resources));
        projectService.updateProject(project.getId(),project);

        // Save all team assignments
        return combinedFindTeamResponseDto;
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

