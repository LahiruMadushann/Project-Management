package com.project_management.services;

import com.project_management.dto.KpiDTO;
import com.project_management.models.Employee;
import com.project_management.models.PerfectEmployee;

import java.util.List;

public interface KpiService {
    List<KpiDTO> calculateKpiForAllEmployees();
    KpiDTO calculateKpiForEmployee(String employeeId);
}
