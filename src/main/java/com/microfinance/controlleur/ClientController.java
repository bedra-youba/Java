// controlleur/ClientController.java
package com.microfinance.controlleur;

import com.microfinance.model.CompteEpargne;
import com.microfinance.model.DemandeAchat;
import com.microfinance.repository.CompteEpargneRepository;
import com.microfinance.repository.DemandeAchatRepository;
import com.microfinance.repository.impl.CompteEpargneRepositoryImpl;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;

import java.sql.SQLException;
import java.util.List;

public class ClientController {

    private final CompteEpargneRepository compteRepo  = new CompteEpargneRepositoryImpl();
    private final DemandeAchatRepository  demandeRepo = new DemandeAchatRepositoryImpl();

    public double consulterSolde(Long idCompte) {
        try { return compteRepo.getSolde(idCompte); }
        catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public List<CompteEpargne> voirMesComptes(Long clientId) {
        try { return compteRepo.trouverParClient(clientId); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean faireDemande(DemandeAchat d) {
        try { demandeRepo.enregistrer(d); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<DemandeAchat> voirMesDemandes(Long clientId) {
        try { return demandeRepo.trouverParClient(clientId); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }
}