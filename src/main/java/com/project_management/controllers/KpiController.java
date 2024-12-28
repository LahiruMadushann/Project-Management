package com.project_management.controllers;

import com.project_management.dto.KpiDTO;
import com.project_management.models.enums.Domain;
import com.project_management.services.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    @Autowired
    private KpiService kpiService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllEmployeesKpi() {
        try {
            List<KpiDTO> kpis = kpiService.calculateKpiForAllEmployees();
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating KPIs for all employees: " + e.getMessage());
        }
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployeeKpi(@PathVariable String employeeId) {
        try {
            KpiDTO kpi = kpiService.calculateKpiForEmployee(employeeId);
            return ResponseEntity.ok(kpi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating KPI for employee " + employeeId + ": " + e.getMessage());
        }
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<?> getKpiByDomain(@PathVariable Domain domain) {
        try {
            List<KpiDTO> kpis = kpiService.calculateKpiByDomain(domain);
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating KPIs for domain " + domain + ": " + e.getMessage());
        }
    }
}