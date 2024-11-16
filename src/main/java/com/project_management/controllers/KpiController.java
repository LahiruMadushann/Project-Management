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
    public ResponseEntity<List<KpiDTO>> getAllEmployeesKpi() {
        List<KpiDTO> kpis = kpiService.calculateKpiForAllEmployees();
        return ResponseEntity.ok(kpis);
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<KpiDTO> getEmployeeKpi(@PathVariable String employeeId) {
        KpiDTO kpi = kpiService.calculateKpiForEmployee(employeeId);
        return ResponseEntity.ok(kpi);
    }
    @GetMapping("/domain/{domain}")
    public ResponseEntity<List<KpiDTO>> getKpiByDomain(@PathVariable Domain domain) {
        List<KpiDTO> kpis = kpiService.calculateKpiByDomain(domain);
        return ResponseEntity.ok(kpis);
    }
}
