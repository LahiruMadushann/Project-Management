package com.project_management.servicesImpl;

import com.project_management.dto.EmployeeDTO;
import com.project_management.models.Employee;
import com.project_management.models.User;
import com.project_management.repositories.EmployeeRepository;
import com.project_management.repositories.UserRepository;
import com.project_management.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    private Random random = new Random();

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO employeeDTO) {
        User currentUser = getCurrentUser();

        Employee employee = new Employee();
        employee.setEmployeeName(employeeDTO.getEmployeeName());
        employee.setSeniority(employeeDTO.getSeniority());
        employee.setCreatedBy(currentUser);

        // Generate employee ID
        String employeeId = generateUniqueEmployeeId(employeeDTO.getEmployeeName());
        employee.setEmployeeId(employeeId);

        Employee savedEmployee = employeeRepository.save(employee);
        return convertToDTO(savedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployee(String employeeId, EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setEmployeeName(employeeDTO.getEmployeeName());
        employee.setSeniority(employeeDTO.getSeniority());

        Employee updatedEmployee = employeeRepository.save(employee);
        return convertToDTO(updatedEmployee);
    }

    @Override
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
        dto.setCreatedByUsername(employee.getCreatedBy().getUsername());
        return dto;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
