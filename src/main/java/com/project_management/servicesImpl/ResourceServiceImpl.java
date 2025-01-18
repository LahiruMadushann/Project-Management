package com.project_management.servicesImpl;

import com.project_management.models.Resource;
import com.project_management.models.enums.ResourceType;
import com.project_management.repositories.ResourceRepository;
import com.project_management.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Resource createResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    @Override
    public Resource updateResource(Long id, Resource resource) {
        Resource temp = resourceRepository.findById(id).orElseThrow();
        long tempId = resource.getId();
        resource.setId(tempId);
        return resourceRepository.save(resource);
    }

    @Override
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }
}
