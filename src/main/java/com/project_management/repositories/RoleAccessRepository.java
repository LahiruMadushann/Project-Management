package com.project_management.repositories;

import com.project_management.models.RoleAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleAccessRepository extends JpaRepository<RoleAccess, Long> {
    List<RoleAccess> findByRoleId(Long roleId);
}
