package com.project_management.controllers;

import com.project_management.dto.ResourceCatalogDto;
import com.project_management.dto.ResourceDTO;
import com.project_management.dto.ResourceMLRequestDTO;
import com.project_management.dto.ResourceMLResponseDTO;
import com.project_management.models.ProjectResourceConfig;
import com.project_management.models.Resource;
import com.project_management.models.enums.BudgetTiers;
import com.project_management.models.enums.ResourceType;
import com.project_management.repositories.ProjectRepository;
import com.project_management.repositories.ProjectResourceConfigRepository;
import com.project_management.services.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ProjectResourceConfigRepository projectResourceConfigRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url.resources}")
    private String resourceMLURL;

    @GetMapping
    public List<Resource> getAllResources() {
        return resourceService.getAllResources();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        Resource resource = resourceService.getResourceById(id);
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody ResourceDTO resource) {
        Resource createdResource = resourceService.createResource(resource);
        return ResponseEntity.status(201).body(createdResource);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Resource>> bulkSave(@RequestBody List<ResourceDTO> list) {
        List<Resource> createdResource = resourceService.bulkSave(list);
        return ResponseEntity.status(201).body(createdResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody ResourceDTO resource) {
        Resource updatedResource = resourceService.updateResource(id, resource);
        return ResponseEntity.ok(updatedResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/predict/{projectId}")
    public ResponseEntity<ResourceMLResponseDTO> predictResources(
            @PathVariable Long projectId,
            @RequestBody ResourceMLRequestDTO requestDto) {
        try {
            System.out.println("in method");
            // Log the request
            log.debug("Sending request to ML service: {}", requestDto);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ResourceMLRequestDTO> entity = new HttpEntity<>(requestDto, headers);

            // Send the request to the ML service
            ResponseEntity<ResourceMLResponseDTO> mlResponse = restTemplate.exchange(
                    resourceMLURL,
                    HttpMethod.POST,
                    entity,
                    ResourceMLResponseDTO.class
            );

            // Check the response status and handle errors
            if (!mlResponse.getStatusCode().is2xxSuccessful() || mlResponse.getBody() == null) {
                throw new RuntimeException("Failed to get prediction from ML service: " +
                        mlResponse.getStatusCode());
            }

            // Save the response to the database
            ResourceMLResponseDTO responseDTO = mlResponse.getBody();
            responseDTO.setProjectBudget(projectRepository.findById(projectId).get().getPredictedBudgetForResources());
            ProjectResourceConfig projectResourceConfig = new ProjectResourceConfig();
            projectResourceConfig.setProjectId(projectId);
            projectResourceConfig.setCloud(responseDTO.getResourceCloud().isPrediction());
            projectResourceConfig.setDb(responseDTO.getResourceDB().isPrediction());
            projectResourceConfig.setAutomation(responseDTO.getResourceAutomation().isPrediction());
            projectResourceConfig.setSecurity(responseDTO.getResourceSecurity().isPrediction());
            projectResourceConfig.setCollaboration(responseDTO.getResourceCollaboration().isPrediction());
            projectResourceConfig.setIde(responseDTO.getResourceIdeTools().isPrediction());

            // Save using JPA repository
            projectResourceConfigRepository.save(projectResourceConfig);


            return mlResponse;

        } catch (HttpStatusCodeException e) {
            System.out.println("ML service error response: {}" + e.getResponseBodyAsString());
            throw new RuntimeException("ML service error: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("Error calling ML service: "+ e);
            throw new RuntimeException("Failed to process resource prediction: " + e.getMessage(), e);
        }
    }

    @GetMapping("/catalog")
    public ResponseEntity<Map<ResourceType, Map<BudgetTiers, List<Resource>>>> getResourcesByBudget(@RequestBody ResourceCatalogDto dto){
        Map<ResourceType, Map<BudgetTiers, List<Resource>>> list = resourceService.predictResourcesByCategory(dto);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/catalog/list/{projectId}")
    public ResponseEntity<List<Map<String, Object>>> getResourcesByBudgetProjectId(@PathVariable Long projectId){
        List<Map<String, Object>> list = resourceService.getResourceMapsByProjectId(projectId);
        return ResponseEntity.ok(list);
    }
}
