package com.project_management.controllers;

import com.project_management.dto.ResourceDTO;
import com.project_management.dto.ResourceMLRequestDTO;
import com.project_management.dto.ResourceMLResponseDTO;
import com.project_management.models.Resource;
import com.project_management.services.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

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

    @PostMapping("/predict")
    public ResponseEntity<ResourceMLResponseDTO> predictResources(@RequestBody ResourceMLRequestDTO requestDto) {
        try {
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

            return mlResponse;

        } catch (HttpStatusCodeException e) {
            log.error("ML service error response: {}", e.getResponseBodyAsString());
            throw new RuntimeException("ML service error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error calling ML service: ", e);
            throw new RuntimeException("Failed to process resource prediction: " + e.getMessage(), e);
        }
    }
}
