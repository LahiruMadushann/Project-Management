package com.project_management.servicesImpl;

import com.project_management.dto.PerfectEmployeeDTO;
import com.project_management.models.PerfectEmployee;
import com.project_management.repositories.PerfectEmployeeRepository;
import com.project_management.services.PerfectEmployeeService;
import com.project_management.services.PerfectRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfectEmployeeServiceImpl implements PerfectEmployeeService {
    @Autowired
    private PerfectEmployeeRepository perfectEmployeeRepository;

    @Autowired
    private PerfectRoleService perfectRoleService;

    @Override
    @Transactional
    public PerfectEmployeeDTO savePerfectEmployee(PerfectEmployeeDTO perfectEmployeeDTO) {
        PerfectEmployee perfectEmployee = convertToEntity(perfectEmployeeDTO);
        perfectRoleService.saveRole(perfectEmployeeDTO.getRoleName());
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

    private PerfectEmployee convertToEntity(PerfectEmployeeDTO dto) {
        PerfectEmployee entity = new PerfectEmployee();
        entity.setEmployeeId(dto.getEmployeeId());
        entity.setEmployeeName(dto.getEmployeeName());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setRoleName(dto.getRoleName());

        // Convert and set skills, experiences, and educations
        entity.setSkills(dto.getSkills());
        entity.setExperiences(dto.getExperiences());
        entity.setEducations(dto.getEducations());

        return entity;
    }

    private PerfectEmployeeDTO convertToDTO(PerfectEmployee entity) {
        PerfectEmployeeDTO dto = new PerfectEmployeeDTO();
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setEmployeeName(entity.getEmployeeName());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setRoleName(entity.getRoleName());

        // Convert and set skills, experiences, and educations
        dto.setSkills(entity.getSkills());
        dto.setExperiences(entity.getExperiences());
        dto.setEducations(entity.getEducations());

        return dto;
    }

    private void updateEmployeeFromDTO(PerfectEmployee employee, PerfectEmployeeDTO dto) {
        employee.setEmployeeName(dto.getEmployeeName());
        employee.setCreatedBy(dto.getCreatedBy());
        employee.setRoleName(dto.getRoleName());

        // Update skills, experiences, and educations
        employee.setSkills(dto.getSkills());
        employee.setExperiences(dto.getExperiences());
        employee.setEducations(dto.getEducations());
    }

}
