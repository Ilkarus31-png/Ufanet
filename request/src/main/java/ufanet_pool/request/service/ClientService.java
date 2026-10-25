package ufanet_pool.request.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ufanet_pool.request.model.Client;
import ufanet_pool.request.repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> findAll() throws Exception {
        return clientRepository.findAll();
    }

    public Client findById(Long id) throws Exception {
        return clientRepository.findById(id);
    }

    public Client create (Client client) throws Exception {
        return clientRepository.save(client);
    }

    public Client update (Long id, Client client) throws Exception {
        return clientRepository.update(id, client);
    }
}
