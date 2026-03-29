
// repository/CompteEpargneRepository.java
package com.microfinance.repository;

import com.microfinance.model.CompteEpargne;
import java.sql.SQLException;
import java.util.List;

public interface CompteEpargneRepository {
    CompteEpargne creer(CompteEpargne compte) throws SQLException;
    CompteEpargne trouverParId(Long id) throws SQLException;
    List<CompteEpargne> trouverParClient(Long clientId) throws SQLException;
    void deposer(Long idCompte, double montant) throws SQLException;
    void retirer(Long idCompte, double montant) throws SQLException;
    double getSolde(Long idCompte) throws SQLException;
}