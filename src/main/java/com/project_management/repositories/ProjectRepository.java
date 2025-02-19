package com.project_management.repositories;

import com.project_management.models.PerfectEmployee;
import com.project_management.models.Project;
import com.project_management.models.enums.RoleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByCreateUserId(Long userId);
    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN p.releaseVersions rv " +
            "LEFT JOIN rv.tasks t " +
            "WHERE t.assignedUser.id = :userId")
    List<Project> findByTasksAssignedUserId(@Param("userId") Long userId);
}
