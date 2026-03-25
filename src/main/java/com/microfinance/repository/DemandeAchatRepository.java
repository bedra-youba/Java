
// repository/DemandeAchatRepository.java
package com.microfinance.repository;

import com.microfinance.model.DemandeAchat;
import java.sql.SQLException;
import java.util.List;

public interface DemandeAchatRepository {
    DemandeAchat enregistrer(DemandeAchat demande) throws SQLException;
    List<DemandeAchat> trouverParClient(Long clientId) throws SQLException;
    List<DemandeAchat> trouverTous() throws SQLException;
    void changerStatut(Long id, String statut) throws SQLException;
}
