package com.project_management.repositories;

import com.project_management.models.TeamAssignment;
import com.project_management.models.TeamAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamAssignmentRepository extends JpaRepository<TeamAssignment, TeamAssignmentId> {
    @Query("SELECT COUNT(ta) FROM TeamAssignment ta WHERE ta.id.employeeId = :employeeId")
    int countAssignmentsByEmployeeId(@Param("employeeId") String employeeId);

    List<TeamAssignment> findByIdProjectId(Long projectId);

    void deleteByIdProjectIdAndIdEmployeeId(Long projectId, String employeeId);
}
