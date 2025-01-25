package com.project_management.repositories;

import com.project_management.models.Resource;
import com.project_management.models.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource,Long> {
    List<Resource> findAllResourcesByResourceType(ResourceType type);

    @Query("SELECT r FROM Resource r WHERE r.resourceType = :type AND r.monthlyCostFloor <= :budget AND r.monthlyCostCeiling >= :budget")
    List<Resource> findResourcesByTypeAndBudget(@Param("type") ResourceType type, @Param("budget") Double budget);
}
