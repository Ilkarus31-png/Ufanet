package ufanet_pool.request.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ufanet_pool.request.model.Client;
import ufanet_pool.request.service.ClientService;



@RestController
@RequestMapping("/api/v0/pool")
public class ClientController {
    private final ClientService clientService;

    public ClientController (ClientService clientService) {
        this.clientService = clientService;
    }
    
    @GetMapping("/clients")
    public List<Client> getAllClients() throws Exception {
        return clientService.findAll();
    }
    
    @GetMapping("/clients/{id}")
    public Client getClientById (@PathVariable Long id) throws Exception{
        return clientService.findById(id);
    }
    
    @PostMapping("/clients")
    @ResponseStatus (HttpStatus.CREATED)
    public Client createClient(@RequestBody Client client) throws Exception {
        return clientService.create(client);
    }
    
    @PatchMapping("/clients/{id}")
    public Client updateClient(
        @PathVariable Long id,
        @RequestBody Client client) throws Exception {

        return clientService.update(id, client);
    }
}
