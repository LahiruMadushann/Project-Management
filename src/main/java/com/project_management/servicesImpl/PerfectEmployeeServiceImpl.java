package com.project_management.servicesImpl;

import com.project_management.dto.PerfectEmployeeDTO;
import com.project_management.dto.PerfectEmployeeEducationDTO;
import com.project_management.dto.PerfectEmployeeExperienceDTO;
import com.project_management.dto.PerfectEmployeeSkillDTO;
import com.project_management.models.*;
import com.project_management.repositories.PerfectEmployeeRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.PerfectEmployeeService;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class PerfectEmployeeServiceImpl implements PerfectEmployeeService {
    @Autowired
    private PerfectEmployeeRepository perfectEmployeeRepository;

    @Autowired
    private PerfectRoleService perfectRoleService;

    @Autowired
    private UserRepository userRepository;

    private Random random = new Random();

    @Override
    @Transactional
    public PerfectEmployeeDTO savePerfectEmployee(PerfectEmployeeDTO perfectEmployeeDTO, Long createdByUserId) {
        PerfectEmployee perfectEmployee = convertToEntity(perfectEmployeeDTO);
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        perfectEmployee.setCreatedBy(createdBy);
        perfectRoleService.saveRole(perfectEmployeeDTO.getRoleName());
        String employeeId = generateUniquePerfectEmployeeId(perfectEmployee.getRoleName());
        perfectEmployee.setEmployeeId(employeeId);
        PerfectEmployee savedEmployee = perfectEmployeeRepository.save(perfectEmployee);
        return convertToDTO(savedEmployee);
    }

    @Override
    @Transactional
    public PerfectEmployeeDTO updatePerfectEmployee(String employeeId, PerfectEmployeeDTO perfectEmployeeDTO) {
        PerfectEmployee existingEmployee = perfectEmployeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Perfect Employee not found"));

        updateEmployeeFromDTO(existingEmployee, perfectEmployeeDTO);
        perfectRoleService.saveRole(perfectEmployeeDTO.getRoleName());
        PerfectEmployee updatedEmployee = perfectEmployeeRepository.save(existingEmployee);
        return convertToDTO(updatedEmployee);
    }

    @Override
    @Transactional
    public void deletePerfectEmployee(String employeeId) {
        perfectEmployeeRepository.deleteById(employeeId);
    }

    @Override
    public PerfectEmployeeDTO getPerfectEmployee(String employeeId) {
        PerfectEmployee perfectEmployee = perfectEmployeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Perfect Employee not found"));
        return convertToDTO(perfectEmployee);
    }

    @Override
    public List<PerfectEmployeeDTO> getAllPerfectEmployees() {
        return perfectEmployeeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    private String generateUniquePerfectEmployeeId(String roleName) {
        String baseId = roleName.replaceAll("\\s+", "").toLowerCase();
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

    private PerfectEmployee convertToEntity(PerfectEmployeeDTO dto) {
        PerfectEmployee entity = new PerfectEmployee();
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setRoleName(dto.getRoleName());

        List<PerfectEmployeeSkill> skills = dto.getSkills().stream()
                .map(skillDto -> {
                    PerfectEmployeeSkill skill = convertSkillToEntity(skillDto);
                    skill.setEmployee(entity);
                    return skill;
                })
                .collect(Collectors.toList());
        entity.setSkills(skills);

        List<PerfectEmployeeExperience> experiences = dto.getExperiences().stream()
                .map(expDto -> {
                    PerfectEmployeeExperience exp = convertExperienceToEntity(expDto);
                    exp.setEmployee(entity);
                    return exp;
                })
                .collect(Collectors.toList());
        entity.setExperiences(experiences);

        List<PerfectEmployeeEducation> educations = dto.getEducations().stream()
                .map(eduDto -> {
                    PerfectEmployeeEducation edu = convertEducationToEntity(eduDto);
                    edu.setEmployee(entity);
                    return edu;
                })
                .collect(Collectors.toList());
        entity.setEducations(educations);

        return entity;
    }

    private PerfectEmployeeDTO convertToDTO(PerfectEmployee entity) {
        PerfectEmployeeDTO dto = new PerfectEmployeeDTO();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setRoleName(entity.getRoleName());
        dto.setCreatedByUserId(entity.getCreatedBy().getId());

        dto.setSkills(entity.getSkills().stream().map(this::convertSkillToDTO).collect(Collectors.toList()));
        dto.setExperiences(entity.getExperiences().stream().map(this::convertExperienceToDTO).collect(Collectors.toList()));
        dto.setEducations(entity.getEducations().stream().map(this::convertEducationToDTO).collect(Collectors.toList()));

        return dto;
    }

    private PerfectEmployeeSkill convertSkillToEntity(PerfectEmployeeSkillDTO dto) {
        PerfectEmployeeSkill entity = new PerfectEmployeeSkill();
        entity.setEmployeeSkillId(dto.getEmployeeSkillId());
        entity.setSkillName(dto.getSkillName());
        entity.setSkillPoints(dto.getSkillPoints());
        entity.setSkillWeight(dto.getSkillWeight());
        return entity;
    }

    private PerfectEmployeeSkillDTO convertSkillToDTO(PerfectEmployeeSkill entity) {
        PerfectEmployeeSkillDTO dto = new PerfectEmployeeSkillDTO();
        dto.setEmployeeSkillId(entity.getEmployeeSkillId());
        dto.setSkillName(entity.getSkillName());
        dto.setSkillPoints(entity.getSkillPoints());
        dto.setSkillWeight(entity.getSkillWeight());
        return dto;
    }

    private PerfectEmployeeExperience convertExperienceToEntity(PerfectEmployeeExperienceDTO dto) {
        PerfectEmployeeExperience entity = new PerfectEmployeeExperience();
        entity.setEmployeeExperienceId(dto.getEmployeeExperienceId());
        entity.setExperienceName(dto.getExperienceName());
        entity.setExperiencePoints(dto.getExperiencePoints());
        entity.setExperienceWeight(dto.getExperienceWeight());
        return entity;
    }

    private PerfectEmployeeExperienceDTO convertExperienceToDTO(PerfectEmployeeExperience entity) {
        PerfectEmployeeExperienceDTO dto = new PerfectEmployeeExperienceDTO();
        dto.setEmployeeExperienceId(entity.getEmployeeExperienceId());
        dto.setExperienceName(entity.getExperienceName());
        dto.setExperiencePoints(entity.getExperiencePoints());
        dto.setExperienceWeight(entity.getExperienceWeight());
        return dto;
    }

    private PerfectEmployeeEducation convertEducationToEntity(PerfectEmployeeEducationDTO dto) {
        PerfectEmployeeEducation entity = new PerfectEmployeeEducation();
        entity.setEmployeeEducationId(dto.getEmployeeEducationId());
        entity.setEducationName(dto.getEducationName());
        entity.setEducationPoints(dto.getEducationPoints());
        entity.setEducationWeight(dto.getEducationWeight());
        return entity;
    }

    private PerfectEmployeeEducationDTO convertEducationToDTO(PerfectEmployeeEducation entity) {
        PerfectEmployeeEducationDTO dto = new PerfectEmployeeEducationDTO();
        dto.setEmployeeEducationId(entity.getEmployeeEducationId());
        dto.setEducationName(entity.getEducationName());
        dto.setEducationPoints(entity.getEducationPoints());
        dto.setEducationWeight(entity.getEducationWeight());
        return dto;
    }

    private void updateEmployeeFromDTO(PerfectEmployee employee, PerfectEmployeeDTO dto) {
        employee.setRoleName(dto.getRoleName());

        List<PerfectEmployeeSkill> skills = dto.getSkills().stream()
                .map(skillDto -> {
                    PerfectEmployeeSkill skill = convertSkillToEntity(skillDto);
                    skill.setEmployee(employee);
                    return skill;
                })
                .collect(Collectors.toList());
        employee.setSkills(skills);

        List<PerfectEmployeeExperience> experiences = dto.getExperiences().stream()
                .map(expDto -> {
                    PerfectEmployeeExperience exp = convertExperienceToEntity(expDto);
                    exp.setEmployee(employee);
                    return exp;
                })
                .collect(Collectors.toList());
        employee.setExperiences(experiences);

        List<PerfectEmployeeEducation> educations = dto.getEducations().stream()
                .map(eduDto -> {
                    PerfectEmployeeEducation edu = convertEducationToEntity(eduDto);
                    edu.setEmployee(employee);
                    return edu;
                })
                .collect(Collectors.toList());
        employee.setEducations(educations);
    }

}
