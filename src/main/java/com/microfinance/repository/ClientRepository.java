

// repository/ClientRepository.java
package com.microfinance.repository;

import com.microfinance.model.Client;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client ajouter(Client client) throws SQLException;
    Optional<Client> trouverParId(Long id) throws SQLException;
    Optional<Client> trouverParNNI(String nni) throws SQLException;
    List<Client> trouverTous() throws SQLException;
    void modifier(Client client) throws SQLException;
    void supprimer(Long id) throws SQLException;
    int getNombreClients() throws SQLException;
}