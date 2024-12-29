package com.project_management.controllers;

import com.project_management.dto.KpiDTO;
import com.project_management.models.enums.Domain;
import com.project_management.services.KpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

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
            return handleException(e);
        }
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployeeKpi(@PathVariable String employeeId) {
        try {
            KpiDTO kpi = kpiService.calculateKpiForEmployee(employeeId);
            return ResponseEntity.ok(kpi);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<?> getKpiByDomain(@PathVariable Domain domain) {
        try {
            List<KpiDTO> kpis = kpiService.calculateKpiByDomain(domain);
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
    }
}