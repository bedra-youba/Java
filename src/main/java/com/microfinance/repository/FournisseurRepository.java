
package com.microfinance.repository;

import com.microfinance.model.Fournisseur;
import java.sql.SQLException;
import java.util.List;

public interface FournisseurRepository {
    Fournisseur ajouter(Fournisseur f) throws SQLException;
    List<Fournisseur> trouverTous() throws SQLException;
    Fournisseur trouverParId(Long id) throws SQLException;
    void modifier(Fournisseur f) throws SQLException;
    void supprimer(Long id) throws SQLException;
}