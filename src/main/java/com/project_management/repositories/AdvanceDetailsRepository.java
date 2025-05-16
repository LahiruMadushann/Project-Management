package com.project_management.repositories;

import com.project_management.models.AdvanceDetails;
import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvanceDetailsRepository extends JpaRepository<AdvanceDetails, Long> {
    AdvanceDetails findByProjectId(Long projectId);
    List<AdvanceDetails> findAllByProjectId(Long projectId);
    AdvanceDetails findTopByProjectIdOrderByIdDesc(Long projectId);
}
