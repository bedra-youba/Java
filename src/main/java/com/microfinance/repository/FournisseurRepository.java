package com.microfinance.repository;

import com.microfinance.model.Fournisseur;
import java.util.List;
import java.util.Optional;

/**
 * Interface Repository pour Fournisseur.
 * Démontre : Interfaces (abstraction), contrat de méthodes (cours Java Avancé)
 */
public interface FournisseurRepository {
    void save(Fournisseur fournisseur);
    void update(Fournisseur fournisseur);
    void delete(Long id);
    Optional<Fournisseur> findById(Long id);
    List<Fournisseur> findAll();
}