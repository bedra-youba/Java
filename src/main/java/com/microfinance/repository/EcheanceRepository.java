

// repository/EcheanceRepository.java
package com.microfinance.repository;

import com.microfinance.model.Echeance;
import java.sql.SQLException;
import java.util.List;

public interface EcheanceRepository {
    Echeance ajouter(Echeance echeance) throws SQLException;
    List<Echeance> trouverParContrat(Long contratId) throws SQLException;
    void marquerPayee(Long idEchange) throws SQLException;
    void supprimer(Long id) throws SQLException;
}
