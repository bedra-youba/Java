package com.microfinance.controlleur;

import com.microfinance.model.ContratMourabaha;
import com.microfinance.model.Echeance;
import com.microfinance.repository.ContratRepository;
import com.microfinance.repository.EcheanceRepository;
import com.microfinance.repository.impl.ContratRepositoryImpl;
import com.microfinance.repository.impl.EcheanceRepositoryImpl;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ContratController {

    private final ContratRepository  contratRepo  = new ContratRepositoryImpl();
    private final EcheanceRepository echeanceRepo = new EcheanceRepositoryImpl();

    public boolean creerContrat(ContratMourabaha contrat) {
        try {
            // Calcul automatique du prix de vente
            contrat.setPrixVenteClient(
                    contrat.getPrixAchatAgence() + contrat.getMargeBeneficiaire()
            );
            contrat.setStatutContrat("Acquisition en cours");

            // Sauvegarde du contrat
            contratRepo.ajouter(contrat);

            // Calcul mensualité
            double mensualite = contrat.getPrixVenteClient() / contrat.getDureeMois();

            // Génération automatique des échéances
            for (int i = 1; i <= contrat.getDureeMois(); i++) {
                Echeance e = new Echeance();
                e.setContrat(contrat);
                e.setNumeroEchange(i);
                e.setDateEchange(contrat.getDateContrat().plusMonths(i));
                e.setMontant(mensualite);
                e.setStatutPaiement("EN_ATTENTE");
                echeanceRepo.ajouter(e);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ContratMourabaha> getContratsByClient(Long clientId) {
        try { return contratRepo.trouverParClient(clientId); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public List<ContratMourabaha> getTousLesContrats() {
        try { return contratRepo.trouverTous(); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public Optional<ContratMourabaha> getContratById(Long id) {
        try { return contratRepo.trouverParId(id); }
        catch (SQLException e) { e.printStackTrace(); return Optional.empty(); }
    }

    public List<Echeance> getEcheancesContrat(Long contratId) {
        try { return echeanceRepo.trouverParContrat(contratId); }
        catch (SQLException e) { e.printStackTrace(); return List.of(); }
    }

    public boolean payerEcheance(Long echeanceId) {
        try { echeanceRepo.marquerPayee(echeanceId); return true; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }
}