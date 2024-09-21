package com.project_management.controllers;

import com.project_management.dto.ClientDTO;
import com.project_management.services.ClientService;
import com.project_management.servicesImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO clientDTO) {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.createClient(clientDTO));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long clientId, @RequestBody ClientDTO clientDTO) {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.updateClient(clientId, clientDTO));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long clientId) {
        userService.adminPermission();
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientDTO> getClient(@PathVariable Long clientId) {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.getClient(clientId));
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @PostMapping("/{clientId}/activate")
    public ResponseEntity<ClientDTO> activateClient(@PathVariable Long clientId) {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.activateClient(clientId));
    }

    @PostMapping("/{clientId}/deactivate")
    public ResponseEntity<ClientDTO> deactivateClient(@PathVariable Long clientId) {
        userService.adminPermission();
        return ResponseEntity.ok(clientService.deactivateClient(clientId));
    }
}
