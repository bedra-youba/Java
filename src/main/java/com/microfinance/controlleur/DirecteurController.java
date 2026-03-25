// controlleur/DirecteurController.java
package com.microfinance.controlleur;

import com.microfinance.model.Agence;
import com.microfinance.model.Agent;
import com.microfinance.repository.AgenceRepository;
import com.microfinance.repository.AgentRepository;
import com.microfinance.repository.ClientRepository;
import com.microfinance.repository.impl.AgenceRepositoryImpl;
import com.microfinance.repository.impl.AgentRepositoryImpl;
import com.microfinance.repository.impl.ClientRepositoryImpl;
import com.microfinance.repository.ContratRepository;
import com.microfinance.repository.impl.ContratRepositoryImpl;

import java.sql.SQLException;
import java.util.List;

public class DirecteurController {

    private final AgenceRepository agenceRepo = new AgenceRepositoryImpl();
    private final AgentRepository  agentRepo  = new AgentRepositoryImpl();
    private final ClientRepository clientRepo = new ClientRepositoryImpl();
    private final ContratRepository contratRepo = new ContratRepositoryImpl();
    public double getCapitalTotal() {
        try { return agenceRepo.getCapitalTotal(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public double getBeneficeParAgence(Long idAgence) {
        try { return agenceRepo.getBeneficeParAgence(idAgence); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int getNombreAgences() {
        try { return agenceRepo.trouverTous().size(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int getNombreAgents() {
        try { return agentRepo.getNombreAgents(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int getNombreClients() {
        try { return clientRepo.getNombreClients(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public List<Agence> getToutesLesAgences() {
        try { return agenceRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean ajouterAgence(Agence a) {
        try { agenceRepo.ajouter(a); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifierAgence(Agence a) {
        try { agenceRepo.modifier(a); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimerAgence(Long id) {
        try { agenceRepo.supprimer(id); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Agent> getTousLesAgents() {
        try { return agentRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean ajouterAgent(Agent a) {
        try {
            if (agentRepo.existeMatricule(a.getMatricule())) return false;
            agentRepo.ajouter(a);
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifierAgent(Agent a) {
        try { agentRepo.modifier(a); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean supprimerAgent(Long id) {
        try { agentRepo.supprimer(id); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }


    public int getNombreContrats() {
        try {
            return contratRepo.getNombreContrats();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}