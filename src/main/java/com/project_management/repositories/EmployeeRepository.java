package com.project_management.repositories;

import com.project_management.models.Employee;
import com.project_management.models.enums.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmployeeName(String employeeName);
    @Query("SELECT e FROM Employee e " +
            "WHERE e.difficultyLevel = :targetLevel " +
            "AND e.id IN (SELECT t.id.employeeId FROM TeamAssignment t WHERE t.id.projectId = :projectId) " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount")
    List<Employee> findSuitableEmployees(@Param("targetLevel") Integer targetLevel, @Param("projectId") Long projectId);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.id IN (SELECT t.id.employeeId FROM TeamAssignment t WHERE t.id.projectId = :projectId) " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount " +
            "ORDER BY ABS(e.difficultyLevel - :targetLevel)")
    List<Employee> findClosestMatchEmployees(@Param("targetLevel") Integer targetLevel, @Param("projectId") Long projectId);
    List<Employee> findByDomain(Domain domain);
}
