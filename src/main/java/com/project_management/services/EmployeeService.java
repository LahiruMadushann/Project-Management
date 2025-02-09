package com.project_management.services;

import com.project_management.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {
    EmployeeDTO createEmployee(EmployeeDTO employeeDTO, Long userId);
    EmployeeDTO updateEmployee(String employeeId, EmployeeDTO employeeDTO);
    void deleteEmployee(String employeeId);
    EmployeeDTO getEmployee(String employeeId);
    List<EmployeeDTO> getAllEmployees();
    String  getAllRolesById(String employeeId);

}
