package ufanet_pool.request.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import ufanet_pool.request.model.Client;

@Repository
public class ClientRepository {

    private final DataSource dataSource;

    public ClientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Client> findAll() throws Exception {
        try (Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                "SELECT id, name, phone, email FROM clients")) {
            
            List<Client> clients = new ArrayList<>();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String phone = resultSet.getString("phone");
                String email = resultSet.getString("email");

                Client client = new Client();
                client.setId(id);
                client.setName(name);
                client.setPhone(phone);
                client.setEmail(email);

                clients.add(client);
            }

            return clients;
        }
    }

    public Client findById (Long id) throws Exception {

        String sql = "SELECT id, name, phone, email FROM clients WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)
            ) {

            preparedStatement.setLong(1, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                    Client client = new Client();
                    client.setId(resultSet.getLong("id"));
                    client.setName(resultSet.getString("name"));
                    client.setPhone(resultSet.getString("phone"));
                    client.setEmail(resultSet.getString("email"));

                    return client;
                }

            }

            return null;
        }
    }
    
    public Client save (Client client) throws Exception {
        String sql = """
        INSERT INTO clients (name, phone, email)
        VALUES (?, ?, ?)
        RETURNING id
        """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, client.getName());
            preparedStatement.setString(2, client.getPhone());
            preparedStatement.setString(3, client.getEmail());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    client.setId(resultSet.getLong("id"));
                }
            }
        }

        return client;
    }

    public Client update (Long id, Client client) throws Exception {
        
        String sql = """
            UPDATE clients
            SET name = ?, phone = ?, email = ?
            WHERE id = ?
            RETURNING id, name, phone, email
            """;

        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, client.getName());
            preparedStatement.setString(2, client.getPhone());
            preparedStatement.setString(3, client.getEmail());
            preparedStatement.setLong(4, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Client updatedClient = new Client();
                    updatedClient.setId(resultSet.getLong("id"));
                    updatedClient.setName(resultSet.getString("name"));
                    updatedClient.setPhone(resultSet.getString("phone"));
                    updatedClient.setEmail(resultSet.getString("email"));
                    return updatedClient;
                }
            }
        }
        return null;
    }
}
