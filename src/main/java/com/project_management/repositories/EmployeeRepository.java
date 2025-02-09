package com.project_management.repositories;

import com.project_management.models.Employee;
import com.project_management.models.enums.Domain;
import com.project_management.models.enums.RoleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {
    Optional<Employee> findByEmployeeName(String employeeName);

    @Query("SELECT e FROM Employee e WHERE e.user.id = :userId")
    Optional<Employee> findByUserId(@Param("userId") Long userId);
    List<Employee> findByDomain(Domain domain);
    @Query("SELECT e FROM Employee e " +
            "WHERE e.employeeId IN (SELECT t.id.employeeId FROM TeamAssignment t WHERE t.id.projectId = :projectId) " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount " +
            "ORDER BY (e.employeeId)")
    List<Employee> findTeamEmployees(@Param("projectId") Long projectId);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.difficultyLevel = :targetLevel " +
            "AND e.employeeId IN (SELECT t.id.employeeId FROM TeamAssignment t WHERE t.id.projectId = :projectId) " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount")
    List<Employee> findSuitableEmployees(@Param("targetLevel") Integer targetLevel, @Param("projectId") Long projectId);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.employeeId IN (SELECT t.id.employeeId FROM TeamAssignment t WHERE t.id.projectId = :projectId) " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount " +
            "ORDER BY ABS(e.difficultyLevel - :targetLevel)")
    List<Employee> findClosestMatchEmployees(@Param("targetLevel") Integer targetLevel, @Param("projectId") Long projectId);

    @Query("SELECT e FROM Employee e WHERE e.roleCategory = :roleCategory")
    List<Employee> findEmployeesByRoleCategory(@Param("roleCategory") RoleCategory roleCategory);

    @Query("SELECT e FROM Employee e " +
            "WHERE e.roleCategory = :roleCategory " +
            "AND e.user IS NOT NULL " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount")
    List<Employee> findEmployeesWithRoleCategoryAndAvailableUser(
            @Param("roleCategory") RoleCategory roleCategory
    );

    @Query("SELECT e FROM Employee e " +
            "WHERE e.roleCategory = :roleCategory " +
            "AND e.difficultyLevel <= :difficultyLevel " +
            "AND e.user IS NOT NULL " +
            "AND (SELECT COUNT(task) FROM Task task WHERE task.assignedUser.id = e.user.id) < e.maximumAssessedCount " +
            "ORDER BY ABS(e.difficultyLevel - :difficultyLevel)")
    List<Employee> findEmployeesByRoleCategoryWithDifficultyMatch(
            @Param("roleCategory") RoleCategory roleCategory,
            @Param("difficultyLevel") Integer difficultyLevel
    );

    @Query("SELECT e.roleName FROM Employee e WHERE e.employeeId = :employeeId")
    String findRolesByEmployeeId(@Param("employeeId") String employeeId);
}