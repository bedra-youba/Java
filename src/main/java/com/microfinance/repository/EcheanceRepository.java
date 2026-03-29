package com.microfinance.repository;

import com.microfinance.model.Echeance;
import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour Echeance.
 * Démontre : Interfaces, abstraction (cours Java Avancé)
 */
public interface EcheanceRepository {
    void save(Echeance echeance);
    void update(Echeance echeance);
    Optional<Echeance> findById(Long id);
    List<Echeance> findByContratId(Long idContrat);
    List<Echeance> findEcheancesEnRetard();
    void payerEcheance(Long idEcheance);
}