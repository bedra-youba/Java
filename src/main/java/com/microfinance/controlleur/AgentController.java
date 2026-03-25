package com.microfinance.controlleur;

import com.microfinance.model.Client;
import com.microfinance.model.CompteEpargne;
import com.microfinance.model.DemandeAchat;
import com.microfinance.model.Fournisseur;
import com.microfinance.repository.*;
import com.microfinance.repository.impl.*;

import java.sql.SQLException;
import java.util.List;

public class AgentController {

    private final ClientRepository        clientRepo      = new ClientRepositoryImpl();
    private final CompteEpargneRepository compteRepo      = new CompteEpargneRepositoryImpl();
    private final DemandeAchatRepository  demandeRepo     = new DemandeAchatRepositoryImpl();
    private final FournisseurRepository   fournisseurRepo = new FournisseurRepositoryImpl();

    // --- Gestion Clients ---
    public boolean ajouterClient(Client c) {
        try { clientRepo.ajouter(c); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean modifierClient(Client c) {
        try { clientRepo.modifier(c); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Client> getTousLesClients() {
        try { return clientRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean nniExiste(String nni) {
        try { return clientRepo.trouverParNNI(nni).isPresent(); }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- Gestion Comptes Epargne ---
    public boolean creerCompteEpargne(CompteEpargne cp) {
        try { compteRepo.creer(cp); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deposer(Long idCompte, double montant) {
        try { compteRepo.deposer(idCompte, montant); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean retirer(Long idCompte, double montant) {
        try { compteRepo.retirer(idCompte, montant); return true; }
        catch (SQLException e) {
            e.printStackTrace(); return false;
        }
    }

    public List<CompteEpargne> getComptesClient(Long clientId) {
        try { return compteRepo.trouverParClient(clientId); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public double getSoldeCompte(Long idCompte) {
        try { return compteRepo.getSolde(idCompte); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    // --- Gestion Demandes Achat ---
    public boolean enregistrerDemande(DemandeAchat d) {
        try { demandeRepo.enregistrer(d); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<DemandeAchat> getToutesLesDemandes() {
        try { return demandeRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean changerStatutDemande(Long id, String statut) {
        try { demandeRepo.changerStatut(id, statut); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // --- Consultation Fournisseurs ---
    public List<Fournisseur> getTousLesFournisseurs() {
        try { return fournisseurRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public int getNombreClients() {
        try { return clientRepo.getNombreClients(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public int getNombreComptes() {
        try { return compteRepo.trouverParClient(0L).size(); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }
}