package com.project_management.servicesImpl;

import com.project_management.dto.ResourceDTO;
import com.project_management.models.Resource;
import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import com.project_management.repositories.ResourceRepository;
import com.project_management.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;


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
        resource.setTier(BudgetTiers.valueOf(dto.getTier()));
        resource.setResourceType(ResourceType.valueOf(dto.getResourceType()));
        resource.setMonthlyCostFloor(dto.getMonthlyCostFloor());
        resource.setMonthlyCostCeiling(dto.getMonthlyCostCeiling());
        resource.setEstimatedCostMonthly(dto.getEstimatedCostMonthly());
        return  resource;
    }
}
