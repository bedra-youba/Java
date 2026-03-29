

// repository/ContratRepository.java
package com.microfinance.repository;

import com.microfinance.model.ContratMourabaha;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ContratRepository {
    ContratMourabaha ajouter(ContratMourabaha contrat) throws SQLException;
    Optional<ContratMourabaha> trouverParId(Long id) throws SQLException;
    List<ContratMourabaha> trouverTous() throws SQLException;
    List<ContratMourabaha> trouverParClient(Long clientId) throws SQLException;
    void modifier(ContratMourabaha contrat) throws SQLException;
    void supprimer(Long id) throws SQLException;
    int getNombreContrats() throws SQLException;
}