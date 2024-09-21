package com.project_management.services;

import com.project_management.dto.ClientDTO;
import java.util.List;

public interface ClientService {
    ClientDTO createClient(ClientDTO clientDTO);
    ClientDTO updateClient(Long clientId, ClientDTO clientDTO);
    void deleteClient(Long clientId);
    ClientDTO getClient(Long clientId);
    List<ClientDTO> getAllClients();
    ClientDTO activateClient(Long clientId);
    ClientDTO deactivateClient(Long clientId);
}