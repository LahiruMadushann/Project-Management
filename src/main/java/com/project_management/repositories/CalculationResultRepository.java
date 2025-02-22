package com.project_management.repositories;

import com.project_management.models.CalculationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationResultRepository extends JpaRepository<CalculationResult, Long> {
    List<CalculationResult> findByProjectId(Long projectId);
}
