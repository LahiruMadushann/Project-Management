package com.project_management.services;

import com.project_management.dto.ResourceCatalogDto;
import com.project_management.dto.ResourceDTO;
import com.project_management.models.Resource;
import com.project_management.models.enums.ResourceType;
import java.util.List;

public interface ResourceService {
    List<Resource> getAllResources();
    List<Resource> getResourcesByType(ResourceType resourceType);
    List<Resource> getResourcesByTypeAndBudget();
    Resource getResourceById(Long id);
    Resource createResource(ResourceDTO dto);
    List<Resource> bulkSave(List<ResourceDTO> list);
    List<Resource> predictResourcesByCategory(ResourceCatalogDto dto);
    Resource updateResource(Long id, ResourceDTO dto);
    void deleteResource(Long id);
}

