package com.project_management.repositories;

import com.project_management.models.Resource;
import com.project_management.models.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource,Long> {
    List<Resource> findAllResourcesByResourceType(ResourceType type);
}
