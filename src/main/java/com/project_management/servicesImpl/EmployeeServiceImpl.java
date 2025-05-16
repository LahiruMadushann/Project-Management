package com.project_management.servicesImpl;

import com.project_management.dto.EmployeeDTO;
import com.project_management.dto.EmployeeEducationDTO;
import com.project_management.dto.EmployeeExperienceDTO;
import com.project_management.dto.EmployeeSkillDTO;
import com.project_management.models.*;
import com.project_management.models.enums.Domain;
import com.project_management.repositories.EmployeeRepository;
import com.project_management.repositories.PerfectEmployeeRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerfectEmployeeRepository perfectEmployeeRepository;

    private Random random = new Random();

    @Override
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO, Long userId) {
        User currentUser = getCurrentUser();
        User associatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        Employee employee = new Employee();
        employee.setEmployeeId(generateUniqueEmployeeId(employeeDTO.getEmployeeName()));
        employee.setEmployeeName(employeeDTO.getEmployeeName());
        employee.setSeniority(employeeDTO.getSeniority());
        employee.setRoleName(employeeDTO.getRoleName());
        employee.setRoleCategory(employeeDTO.getRoleCategory());
        employee.setCreatedBy(currentUser);
        employee.setUser(associatedUser);;
        employee.setMaximumAssessedCount(employeeDTO.getMaximumAssessedCount());
        employee.setDifficultyLevel(employeeDTO.getDifficultyLevel());
        employee.setDomain(Domain.valueOf(employeeDTO.getDomain()));

        // Generate employee ID
        String employeeId = generateUniqueEmployeeId(employeeDTO.getEmployeeName());
        employee.setEmployeeId(employeeId);

        employee.setSkills(mapSkills(employeeDTO.getSkills(), employee));
        employee.setExperiences(mapExperiences(employeeDTO.getExperiences(), employee));
        employee.setEducations(mapEducations(employeeDTO.getEducations(), employee));

        /**
         * calculate initial kpi
         */
        calculateInitialKpi(employee);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeDTO updateEmployee(String employeeId, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (employeeDTO.getEmployeeName() != null) {
            employee.setEmployeeName(employeeDTO.getEmployeeName());
        }
        if (employeeDTO.getSeniority() != null) {
            employee.setSeniority(employeeDTO.getSeniority());
        }
        if (employeeDTO.getRoleName() != null) {
            employee.setRoleName(employeeDTO.getRoleName());
        }

        if (employeeDTO.getRoleCategory() != null) {
            employee.setRoleCategory(employeeDTO.getRoleCategory());
        }

        if (employeeDTO.getMaximumAssessedCount() != null) {
            employee.setMaximumAssessedCount(employeeDTO.getMaximumAssessedCount());
        }

        if (employeeDTO.getDomain() != null) {
            employee.setDomain(Domain.valueOf(employeeDTO.getDomain()));
        }



        if (employeeDTO.getSkills() != null) {
            updateSkills(employee, employeeDTO.getSkills());
        }

        if (employeeDTO.getExperiences() != null) {
            updateExperiences(employee, employeeDTO.getExperiences());
        }

        if (employeeDTO.getEducations() != null) {
            updateEducations(employee, employeeDTO.getEducations());
        }




        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    private void updateSkills(Employee employee, List<EmployeeSkillDTO> skillDTOs) {
        Map<String, EmployeeSkill> existingSkills = employee.getSkills().stream()
                .collect(Collectors.toMap(EmployeeSkill::getSkillName, s -> s));

        List<EmployeeSkill> updatedSkills = new ArrayList<>();

        for (EmployeeSkillDTO skillDTO : skillDTOs) {
            EmployeeSkill skill = existingSkills.get(skillDTO.getSkillName());
            if (skill == null) {
                skill = new EmployeeSkill();
                skill.setSkillName(skillDTO.getSkillName());
                skill.setEmployee(employee);
            }
            skill.setSkillPoints(skillDTO.getSkillPoints());
            updatedSkills.add(skill);
            existingSkills.remove(skillDTO.getSkillName());
        }

        employee.getSkills().removeAll(existingSkills.values());

        employee.getSkills().addAll(updatedSkills);
    }

    private void updateExperiences(Employee employee, List<EmployeeExperienceDTO> experienceDTOs) {
        Map<String, EmployeeExperience> existingExperiences = employee.getExperiences().stream()
                .collect(Collectors.toMap(EmployeeExperience::getExperienceName, e -> e));

        List<EmployeeExperience> updatedExperiences = new ArrayList<>();

        for (EmployeeExperienceDTO experienceDTO : experienceDTOs) {
            EmployeeExperience experience = existingExperiences.get(experienceDTO.getExperienceName());
            if (experience == null) {
                experience = new EmployeeExperience();
                experience.setExperienceName(experienceDTO.getExperienceName());
                experience.setEmployee(employee);
            }
            experience.setExperiencePoints(experienceDTO.getExperiencePoints());
            updatedExperiences.add(experience);
            existingExperiences.remove(experienceDTO.getExperienceName());
        }

        employee.getExperiences().removeAll(existingExperiences.values());

        employee.getExperiences().addAll(updatedExperiences);
    }

    private void updateEducations(Employee employee, List<EmployeeEducationDTO> educationDTOs) {
        Map<String, EmployeeEducation> existingEducations = employee.getEducations().stream()
                .collect(Collectors.toMap(EmployeeEducation::getEducationName, e -> e));

        List<EmployeeEducation> updatedEducations = new ArrayList<>();

        for (EmployeeEducationDTO educationDTO : educationDTOs) {
            EmployeeEducation education = existingEducations.get(educationDTO.getEducationName());
            if (education == null) {
                education = new EmployeeEducation();
                education.setEducationName(educationDTO.getEducationName());
                education.setEmployee(employee);
            }
            education.setEducationPoints(educationDTO.getEducationPoints());
            updatedEducations.add(education);
            existingEducations.remove(educationDTO.getEducationName());
        }

        employee.getEducations().removeAll(existingEducations.values());

        employee.getEducations().addAll(updatedEducations);
    }

    @Override
    @Transactional
    public void deleteEmployee(String employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    @Override
    public EmployeeDTO getEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToDTO(employee);
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public String getAllRolesById(String employeeId) {
        return employeeRepository.findRolesByEmployeeId(employeeId);
    }

    private String generateUniqueEmployeeId(String employeeName) {
        String baseId = employeeName.replaceAll("\\s+", "").toLowerCase();
        String timestamp = Long.toString(Instant.now().toEpochMilli());
        String randomNum = String.format("%04d", random.nextInt(10000));

        String employeeId = baseId + "-" + timestamp + "-" + randomNum;

        // Truncate if the ID is too long (adjust the max length as needed)
        int maxLength = 50;
        if (employeeId.length() > maxLength) {
            employeeId = employeeId.substring(0, maxLength);
        }

        return employeeId;
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setEmployeeName(employee.getEmployeeName());
        dto.setSeniority(employee.getSeniority());
        dto.setRoleName(employee.getRoleName());
        dto.setRoleCategory(employee.getRoleCategory());
        dto.setCreatedByUsername(employee.getCreatedBy().getUsername());
        dto.setUserId(employee.getUser().getId());
        dto.setMaximumAssessedCount(employee.getMaximumAssessedCount());
        dto.setDifficultyLevel(employee.getDifficultyLevel());
        dto.setDomain(String.valueOf(employee.getDomain()));
        dto.setSalary(employee.getSalary());
        dto.setKpi(employee.getKpi());

        dto.setSkills(employee.getSkills().stream().map(this::convertToSkillDTO).collect(Collectors.toList()));
        dto.setExperiences(employee.getExperiences().stream().map(this::convertToExperienceDTO).collect(Collectors.toList()));
        dto.setEducations(employee.getEducations().stream().map(this::convertToEducationDTO).collect(Collectors.toList()));
        return dto;
    }


    private EmployeeSkillDTO convertToSkillDTO(EmployeeSkill skill) {
        EmployeeSkillDTO dto = new EmployeeSkillDTO();
        dto.setEmployeeSkillId(skill.getEmployeeSkillId());
        dto.setSkillName(skill.getSkillName());
        dto.setSkillPoints(skill.getSkillPoints());
        return dto;
    }

    private EmployeeExperienceDTO convertToExperienceDTO(EmployeeExperience experience) {
        EmployeeExperienceDTO dto = new EmployeeExperienceDTO();
        dto.setEmployeeExperienceId(experience.getEmployeeExperienceId());
        dto.setExperienceName(experience.getExperienceName());
        dto.setExperiencePoints(experience.getExperiencePoints());
        return dto;
    }

    private EmployeeEducationDTO convertToEducationDTO(EmployeeEducation education) {
        EmployeeEducationDTO dto = new EmployeeEducationDTO();
        dto.setEmployeeEducationId(education.getEmployeeEducationId());
        dto.setEducationName(education.getEducationName());
        dto.setEducationPoints(education.getEducationPoints());
        return dto;
    }

    private List<EmployeeSkill> mapSkills(List<EmployeeSkillDTO> skillDTOs, Employee employee) {
        return skillDTOs.stream().map(dto -> {
            EmployeeSkill skill = new EmployeeSkill();
            skill.setSkillName(dto.getSkillName());
            skill.setSkillPoints(dto.getSkillPoints());
            skill.setEmployee(employee);
            return skill;
        }).collect(Collectors.toList());
    }

    private List<EmployeeExperience> mapExperiences(List<EmployeeExperienceDTO> experienceDTOs, Employee employee) {
        return experienceDTOs.stream().map(dto -> {
            EmployeeExperience experience = new EmployeeExperience();
            experience.setExperienceName(dto.getExperienceName());
            experience.setExperiencePoints(dto.getExperiencePoints());
            experience.setEmployee(employee);
            return experience;
        }).collect(Collectors.toList());
    }

    private List<EmployeeEducation> mapEducations(List<EmployeeEducationDTO> educationDTOs, Employee employee) {
        return educationDTOs.stream().map(dto -> {
            EmployeeEducation education = new EmployeeEducation();
            education.setEducationName(dto.getEducationName());
            education.setEducationPoints(dto.getEducationPoints());
            education.setEmployee(employee);
            return education;
        }).collect(Collectors.toList());
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * calculate the initial KPI
     * @param employee
     */
    private void calculateInitialKpi(Employee employee) {
        // Fetch the PerfectEmployee based on the given employee's role
        PerfectEmployee perfectEmployee = perfectEmployeeRepository.findByRoleName(employee.getRoleName());
        if (perfectEmployee == null) {
            throw new IllegalArgumentException("Perfect employee not found for role: " + employee.getRoleName());
        }

        // Initialize total scores
        double employeeTotal = 0.0;
        double perfectTotal = 0.0;

        // Calculate Skill Score
        for (EmployeeSkill employeeSkill : employee.getSkills()) {
            PerfectEmployeeSkill perfectSkill = perfectEmployee.getSkills().stream()
                    .filter(s -> s.getSkillName().equalsIgnoreCase(employeeSkill.getSkillName()))
                    .findFirst()
                    .orElse(null);

            if (perfectSkill != null) {
                employeeTotal += employeeSkill.getSkillPoints() * perfectSkill.getSkillWeight();
                perfectTotal += perfectSkill.getSkillPoints() * perfectSkill.getSkillWeight();
            }
        }

        // Calculate Experience Score
        for (EmployeeExperience employeeExperience : employee.getExperiences()) {
            PerfectEmployeeExperience perfectExperience = perfectEmployee.getExperiences().stream()
                    .filter(e -> e.getExperienceName().equalsIgnoreCase(employeeExperience.getExperienceName()))
                    .findFirst()
                    .orElse(null);

            if (perfectExperience != null) {
                employeeTotal += employeeExperience.getExperiencePoints() * perfectExperience.getExperienceWeight();
                perfectTotal += perfectExperience.getExperiencePoints() * perfectExperience.getExperienceWeight();
            }
        }

        // Calculate Education Score
        for (EmployeeEducation employeeEducation : employee.getEducations()) {
            PerfectEmployeeEducation perfectEducation = perfectEmployee.getEducations().stream()
                    .filter(e -> e.getEducationName().equalsIgnoreCase(employeeEducation.getEducationName()))
                    .findFirst()
                    .orElse(null);

            if (perfectEducation != null) {
                employeeTotal += employeeEducation.getEducationPoints() * perfectEducation.getEducationWeight();
                perfectTotal += perfectEducation.getEducationPoints() * perfectEducation.getEducationWeight();
            }
        }

        // Avoid division by zero
        if (perfectTotal == 0) {
            employee.setKpi(0.0);
        }

        // Calculate KPI as a percentage
        employee.setKpi((employeeTotal / perfectTotal) * 100);
    }

}
