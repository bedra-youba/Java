package com.microfinance.repository;

import com.microfinance.model.Client;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client save(Client client) throws SQLException;
    Optional<Client> findById(Long id) throws SQLException;
    List<Client> findAll() throws SQLException;
    void update(Client client) throws SQLException;
    void delete(Long id) throws SQLException;
    Optional<Client> findByNNI(String nni) throws SQLException;
    List<Client> rechercherParNom(String nom) throws SQLException;
}