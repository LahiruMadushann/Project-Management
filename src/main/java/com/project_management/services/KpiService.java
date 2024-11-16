package com.project_management.services;

import com.project_management.dto.KpiDTO;
import com.project_management.models.Employee;
import com.project_management.models.PerfectEmployee;
import com.project_management.models.enums.Domain;

import java.util.List;

public interface KpiService {
    List<KpiDTO> calculateKpiForAllEmployees();
    KpiDTO calculateKpiForEmployee(String employeeId);
    List<KpiDTO> calculateKpiByDomain(Domain domain);
}
