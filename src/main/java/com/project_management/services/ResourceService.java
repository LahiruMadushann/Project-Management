package com.project_management.services;

import com.project_management.dto.ResourceCatalogDto;
import com.project_management.dto.ResourceDTO;
import com.project_management.models.Resource;
import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import java.util.List;
import java.util.Map;

public interface ResourceService {
    List<Resource> getAllResources();
    List<Resource> getResourcesByType(ResourceType resourceType);
    List<Resource> getResourcesByTypeAndBudget();
    Resource getResourceById(Long id);
    Resource createResource(ResourceDTO dto);
    List<Resource> bulkSave(List<ResourceDTO> list);
    Map<ResourceType, Map<BudgetTiers, List<Resource>>> predictResourcesByCategory(ResourceCatalogDto dto);
    List<Map<String, Object>> getResourceMapsByProjectId(Long projectId);
    Resource updateResource(Long id, ResourceDTO dto);
    void deleteResource(Long id);
}

