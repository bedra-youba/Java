
// repository/AgentRepository.java
package com.microfinance.repository;

import com.microfinance.model.Agent;
import java.sql.SQLException;
import java.util.List;

public interface AgentRepository {
    Agent ajouter(Agent agent) throws SQLException;
    Agent trouverParId(Long id) throws SQLException;
    List<Agent> trouverTous() throws SQLException;
    void modifier(Agent agent) throws SQLException;
    void supprimer(Long id) throws SQLException;
    boolean existeMatricule(String matricule) throws SQLException;
    int getNombreAgents() throws SQLException;
}