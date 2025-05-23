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
import java.time.LocalDate;
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
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SubTaskRepository subTaskRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ReleaseVersionService releaseVersionService;

    @Value("${ml.service.url.budget}")
    private String budgetUrl;


    private double formatNonNegative(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        double formattedValue = Double.parseDouble(df.format(value));
        return Math.max(0.0, formattedValue); // Ensures value is not negative
    }

    /**
     * Logic implementation of finding employee pool
     * 1. get project
     * 2. get employee
     * 3. employee filtering
     * 4. map kpi
     * @param findTeamDTO
     * @return
     */
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


        AdvanceDetails advanceDetails = advanceDetailsRepository.findTopByProjectIdOrderByIdDesc(findTeamDTO.getProjectId());
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
                    TeamAssignment assignment = createTeamAssignment(findTeamDTO.getProjectId(), selectedEmployee ,kpiMap);
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
        ProjectBudgetGraphDto graphDto = new ProjectBudgetGraphDto();
        if(advanceDetails != null) {
            BudgetMLRequestDto requestDto = new BudgetMLRequestDto();
            requestDto.setDomain(advanceDetails.getDomain());
            requestDto.setMethodology(advanceDetails.getMethodology() == 2 ? "Agile" : "Waterfall");
            requestDto.setDevCount(devCount);
            requestDto.setQaCount(qaCount);
            requestDto.setPmCount(pmCount);
            requestDto.setBaCount(baCount);
            requestDto.setDevOpsCount(devopsCount);
            requestDto.setMiscellaneousExpenses(advanceDetails.getOtherExpenses());
            requestDto.setProfitMargin(advanceDetails.getExpectedProfit());
            requestDto.setResourceCloud(latestConfig.getCloud() ? 1 : 0);
            requestDto.setResourceAutomation(latestConfig.getAutomation() ? 1 : 0);
            requestDto.setResourceDb(latestConfig.getDb() ? 1 : 0);
            requestDto.setResourceSecurity(latestConfig.getSecurity() ? 1 : 0);
            requestDto.setResourceIdeTools(latestConfig.getIde() ? 1 : 0);
            requestDto.setResourceCollaboration(latestConfig.getCollaboration() ? 1 : 0);

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

            fullBudget = Math.round(mlResponse.getBody().getBudget() / 10000.0) * 100000;

            salaryWeight = (salary / fullBudget) * 100;
            profitWeight = advanceDetails.getExpectedProfit();
            profit = (fullBudget / 100) * profitWeight;
            misWeight = advanceDetails.getOtherExpenses();
            mis = (fullBudget / 100) * misWeight;
            resourcesWeight = fullWeight - (profitWeight + misWeight + salaryWeight);
            resources = (fullBudget / 100) * resourcesWeight;


// Format all decimal values before setting them
//            ProjectBudgetGraphDto graphDto = new ProjectBudgetGraphDto();
            // Helper method to format values and ensure they're not negative


// Now we can use this helper method in your existing code
            graphDto.setProjectId(project.getId());
            graphDto.setFullBudget((long) formatNonNegative(fullBudget));
            graphDto.setFullWeight(formatNonNegative(fullWeight));
            graphDto.setExpectedBudget(project.getBudget() != null ?
                    BigDecimal.valueOf(formatNonNegative(project.getBudget().doubleValue())) : null);
            graphDto.setProfit(formatNonNegative(profit));
            graphDto.setProfitWeight(formatNonNegative(profitWeight));
            graphDto.setSalary(formatNonNegative(salary));
            graphDto.setSalaryWeigh(formatNonNegative(salaryWeight));
            graphDto.setOtherExpenses(formatNonNegative(mis));
            graphDto.setOtherExpensesWight(formatNonNegative(misWeight));
            graphDto.setResources(formatNonNegative(resources));
            graphDto.setResourcesWeight(formatNonNegative(resourcesWeight));
            graphDto.setUnassigned(formatNonNegative(unassigned));
            graphDto.setUnassginedWeight(formatNonNegative(unassginedWeight));

            graphDto.setExpectedBudget(project.getPredictedBudgetForResources());
            graphDto.setBudgetRisk(
                    BigDecimal.valueOf(fullBudget).compareTo(project.getBudget()) < 0 ? "LOW" : "HIGH"
            );

            List<ReleaseVersionDTO> releases  = releaseVersionService.getReleaseVersionsByProjectId(project.getId());
            LocalDate farthestDeadline = releases.stream()
                    .flatMap(release -> release.getTasks().stream()) // Stream<TaskDTO>
                    .map(TaskDTO::getDeadline) // Stream<LocalDate>
                    .filter(date -> date != null) // skip nulls
                    .max(Comparator.naturalOrder()) // find the latest date
                    .orElse(null);
            LocalDate extendedDeadline = farthestDeadline.plusWeeks(2);

            if (extendedDeadline.isBefore(project.getDeadline())) {
               graphDto.setDateRisk("LOW");
            }else if (farthestDeadline.isBefore(project.getDeadline())) {
                graphDto.setDateRisk("MEDIUM");
            } else if (extendedDeadline.isAfter(project.getDeadline())) {
                graphDto.setDateRisk("HIGH");
            } else {
                graphDto.setDateRisk("MEDIUM");
            }

            graphDto.setPredictedDate(String.valueOf(extendedDeadline));


        }

        CombinedFindTeamResponseDto combinedFindTeamResponseDto = new CombinedFindTeamResponseDto();
        combinedFindTeamResponseDto.setEmployees(teamAssignmentRepository.saveAll(selectedTeam));
        if (advanceDetails != null) {
            combinedFindTeamResponseDto.setGraph(graphDto);

            project.setPredictedBudgetForResources(BigDecimal.valueOf(resources));
            projectService.updateProject(project.getId(), project);
        }
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

    private TeamAssignment createTeamAssignment(Long projectId, EmployeeDTO employee, Map<String, KpiDTO> kpiMap) {
        TeamAssignment assignment = new TeamAssignment();

        TeamAssignmentId id = new TeamAssignmentId();
        id.setProjectId(projectId);
        id.setEmployeeId(employee.getEmployeeId());

        assignment.setId(id);
        assignment.setEmployeeName(employee.getEmployeeName());
        assignment.setRoleName(employee.getRoleName());
        assignment.setKpi(kpiMap.get(employee.getEmployeeId()).getExperienceKpi());

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
    public List<TeamAssignmentDTO> getTeamByProjectId(Long projectId) {
        List<TeamAssignment> teamAssignments = teamAssignmentRepository.findByIdProjectId(projectId);

        return teamAssignments.stream().map(assignment -> {
            TeamAssignmentDTO dto = new TeamAssignmentDTO();
            dto.setId(assignment.getId());
            dto.setEmployeeName(assignment.getEmployeeName());
            dto.setRoleName(assignment.getRoleName());
            dto.setCreatedAt(assignment.getCreatedAt());
            dto.setUpdatedAt(assignment.getUpdatedAt());
            dto.setKpi(assignment.getKpi());

            String employeeId = assignment.getId().getEmployeeId();
            Employee employee = employeeRepository.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

            User user = employee.getUser();
            if (user != null) {
                Long userId = user.getId();

                long taskCount = taskRepository.countByAssignedUserId(userId);
                long subTaskCount = subTaskRepository.countByAssignedUserId(userId);
                long totalAssignments = taskCount + subTaskCount;

                Integer maxAssessedCount = employee.getMaximumAssessedCount();
                if (maxAssessedCount != null && maxAssessedCount > 0) {
                    double percentage = ((double) totalAssignments / maxAssessedCount) * 100;
                    double roundedPercentage = Math.min(Math.round(percentage * 100.0) / 100.0, 100.00);
                    dto.setExhaustedPercentage(roundedPercentage);
                }

            }

            return dto;
        }).collect(Collectors.toList());
    }

}

