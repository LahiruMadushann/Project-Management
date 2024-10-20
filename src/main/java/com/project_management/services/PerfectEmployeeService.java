package com.project_management.services;

import com.project_management.dto.PerfectEmployeeDTO;

import java.util.List;

public interface PerfectEmployeeService {
    PerfectEmployeeDTO savePerfectEmployee(PerfectEmployeeDTO perfectEmployeeDTO);
    PerfectEmployeeDTO updatePerfectEmployee(String employeeId, PerfectEmployeeDTO perfectEmployeeDTO);
    void deletePerfectEmployee(String employeeId);
    PerfectEmployeeDTO getPerfectEmployee(String employeeId);
    List<PerfectEmployeeDTO> getAllPerfectEmployees();
}
