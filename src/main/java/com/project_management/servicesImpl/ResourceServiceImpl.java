package com.project_management.servicesImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project_management.dto.ResourceCatalogDto;
import com.project_management.dto.ResourceDTO;
import com.project_management.models.ProjectResourceMap;
import com.project_management.models.Resource;
import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import com.project_management.repositories.ProjectResourceMapRepository;
import com.project_management.repositories.ResourceRepository;
import com.project_management.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectResourceMapRepository projectResourceMapRepository;


    @Override
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Override
    public List<Resource> getResourcesByType(ResourceType resourceType) {
        return resourceRepository.findAllResourcesByResourceType(resourceType);
    }

    @Override
    public List<Resource> getResourcesByTypeAndBudget() {
        return List.of();
    }

    @Override
    public Resource getResourceById(Long id) {
        return resourceRepository.findById(id).orElseThrow();
    }

    @Override
    public Resource createResource(ResourceDTO dto) {
        return resourceRepository.save(dtoToRes(dto));
    }

    @Override
    public List<Resource> bulkSave(List<ResourceDTO> list) {
        List<Resource> resources = list.stream()
                .map(this::dtoToRes)
                .collect(Collectors.toList());
        return resourceRepository.saveAll(resources);
    }

    @Override
    public Map<ResourceType, Map<BudgetTiers, List<Resource>>> predictResourcesByCategory(ResourceCatalogDto dto) {
        // Calculate the monthly budget
        double monthlyBudget = dto.getBudget() / (dto.getServiceYears() * 12);

        // Define category budgets in a map
        Map<ResourceType, Double> budgetMap = Map.of(
                ResourceType.CLOUD, monthlyBudget * dto.getCloudPercentage() / 100,
                ResourceType.DB, monthlyBudget * dto.getDbPercentage() / 100,
                ResourceType.IDE, monthlyBudget * dto.getIdePercentage() / 100,
                ResourceType.AUTOMATION, monthlyBudget * dto.getAutomationPercentage() / 100,
                ResourceType.SECURITY, monthlyBudget * dto.getSecurityPercentage() / 100,
                ResourceType.COLLABORATION, monthlyBudget * dto.getCollaborationPercentage() / 100
        );

        // Initialize a nested map to ensure all resource types and budget tiers exist
        Map<ResourceType, Map<BudgetTiers, List<Resource>>> resourceMap = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            Map<BudgetTiers, List<Resource>> tierMap = new HashMap<>();
            for (BudgetTiers tier : BudgetTiers.values()) {
                tierMap.put(tier, new ArrayList<>()); // Ensure all budget tiers exist
            }
            resourceMap.put(type, tierMap);
        }

        // Fetch resources and populate the map
        budgetMap.forEach((type, budget) -> {
            List<Resource> resources = resourceRepository.findResourcesByTypeAndBudget(type, budget);
            resources.forEach(resource ->
                    resourceMap.get(type).get(resource.getTier()).add(resource) // Group by tier within each type
            );
        });

        // Combine requestDto and resourceMap
        Map<String, Object> combinedData = new HashMap<>();
        combinedData.put("requestDto", dto); // Add requestDto
        combinedData.put("resourceMap", resourceMap); // Add resourceMap

        try {
            String jsonString = objectMapper.writeValueAsString(combinedData); // Convert combined data to JSON
            ProjectResourceMap projectResourceMap = new ProjectResourceMap();
            projectResourceMap.setProjectId(dto.getProjectId());
            projectResourceMap.setMapString(jsonString);
            projectResourceMapRepository.save(projectResourceMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // Handle error
        }

        return resourceMap;
    }

    @Override
    public List<Map<String, Object>> getResourceMapsByProjectId(Long projectId) {
        List<ProjectResourceMap> resourceMaps = projectResourceMapRepository.findByProjectId(projectId);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (ProjectResourceMap resourceMap : resourceMaps) {
            try {
                Map<String, Object> map = objectMapper.readValue(
                        resourceMap.getMapString(),
                        new TypeReference<>() {}
                );
                resultList.add(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return resultList;
    }




    @Override
    public Resource updateResource(Long id, ResourceDTO dto) {
        Resource temp = resourceRepository.findById(id).orElseThrow();
        long tempId = temp.getId();
        dto.setId(tempId);
        return resourceRepository.save(dtoToRes(dto));
    }

    @Override
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    public Resource dtoToRes(ResourceDTO dto){
        Resource resource = new Resource();
        resource.setId(dto.getId());
        resource.setName(dto.getName());
        resource.setTier(BudgetTiers.valueOf(dto.getTier()));
        resource.setResourceType(ResourceType.valueOf(dto.getResourceType()));
        resource.setMonthlyCostFloor(dto.getMonthlyCostFloor());
        resource.setMonthlyCostCeiling(dto.getMonthlyCostCeiling());
        resource.setEstimatedCostMonthly(dto.getEstimatedCostMonthly());
        return  resource;
    }
}
