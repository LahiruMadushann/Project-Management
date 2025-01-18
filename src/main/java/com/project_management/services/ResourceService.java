package com.project_management.services;

import com.project_management.models.Resource;
import com.project_management.models.enums.ResourceType;
import java.util.List;

public interface ResourceService {
    List<Resource> getAllResources();
    List<Resource> getResourcesByType(ResourceType resourceType);
    List<Resource> getResourcesByTypeAndBudget();
    Resource getResourceById(Long id);
    Resource createResource(Resource resource);
    Resource updateResource(Long id, Resource resource);
    void deleteResource(Long id);
}

