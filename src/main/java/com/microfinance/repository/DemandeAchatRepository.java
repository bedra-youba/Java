package com.microfinance.repository;

import com.microfinance.model.DemandeAchat;
import java.util.List;
import java.util.Optional;

public interface DemandeAchatRepository {

    void save(DemandeAchat demande);
    Optional<DemandeAchat> findById(Long id);
    List<DemandeAchat> findAll();
    List<DemandeAchat> findByClientId(Long idClient);

    // Met à jour le statut seul (ACCEPTEE, BIEN_RECU, CONTRAT_CREE, REFUSEE)
    void updateStatut(Long idDemande, String nouveauStatut);

    // Met à jour le fournisseur + statut en même temps (COMMANDE_FOURNISSEUR)
    void updateFournisseurEtStatut(Long idDemande, Long idFournisseur, String nouveauStatut);
}