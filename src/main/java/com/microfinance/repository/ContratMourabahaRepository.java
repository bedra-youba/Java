package com.microfinance.repository;

import com.microfinance.model.ContratMourabaha;
import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour ContratMourabaha.
 * Démontre : Interfaces, polymorphisme (cours Java Avancé)
 */
public interface ContratMourabahaRepository {
    void save(ContratMourabaha contrat);
    void update(ContratMourabaha contrat);
    void delete(Long id);
    Optional<ContratMourabaha> findById(Long id);
    List<ContratMourabaha> findAll();
    List<ContratMourabaha> findByClientId(Long idClient);
    double getBeneficeTotal();
}