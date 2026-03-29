package com.microfinance.repository.impl;

import com.microfinance.model.ContratMourabaha;
import com.microfinance.model.Echeance;
import com.microfinance.repository.EcheanceRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de EcheanceRepository.
 *
 * Démontre (cours Java Avancé) :
 *  - Implémentation d'interface (implements)
 *  - JDBC : PreparedStatement, ResultSet, Timestamp
 *  - try-with-resources (fermeture auto connexion HikariCP)
 *  - Gestion des exceptions
 *  - Collections : List, ArrayList
 *  - Logique métier : clôture automatique du contrat
 */
public class EcheanceRepositoryImpl implements EcheanceRepository {

    // ─────────────────────────────────────────────────────
    // SAVE
    // ─────────────────────────────────────────────────────
    @Override
    public void save(Echeance echeance) {
        String sql = "INSERT INTO Echeance (numeroEchange, dateEchange, montant, statutPaiement, idContrat) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING idEchange";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, echeance.getNumeroEchange());
            ps.setTimestamp(2, Timestamp.valueOf(echeance.getDateEchange()));
            ps.setDouble(3, echeance.getMontant());
            ps.setString(4, echeance.getStatutPaiement());
            ps.setLong(5, echeance.getContrat().getIdContrat());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                echeance.setIdEchange(rs.getLong("idEchange"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur sauvegarde échéance : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────
    @Override
    public void update(Echeance echeance) {
        String sql = "UPDATE Echeance SET statutPaiement=?, dateEchange=?, montant=? WHERE idEchange=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, echeance.getStatutPaiement());
            ps.setTimestamp(2, Timestamp.valueOf(echeance.getDateEchange()));
            ps.setDouble(3, echeance.getMontant());
            ps.setLong(4, echeance.getIdEchange());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour échéance : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────────────────
    @Override
    public Optional<Echeance> findById(Long id) {
        String sql = "SELECT * FROM Echeance WHERE idEchange=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche échéance : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────
    // FIND BY CONTRAT — Collections
    // ─────────────────────────────────────────────────────
    @Override
    public List<Echeance> findByContratId(Long idContrat) {
        List<Echeance> liste = new ArrayList<>();
        String sql = "SELECT * FROM Echeance WHERE idContrat=? ORDER BY numeroEchange";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idContrat);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération échéances : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────
    // ECHÉANCES EN RETARD — logique métier + SQL
    // ─────────────────────────────────────────────────────
    @Override
    public List<Echeance> findEcheancesEnRetard() {
        List<Echeance> liste = new ArrayList<>();
        String sql = "SELECT * FROM Echeance " +
                "WHERE dateEchange < NOW() AND statutPaiement = 'IMPAYEE' " +
                "ORDER BY dateEchange";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération échéances en retard : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────
    // PAYER UNE ÉCHÉANCE — méthode métier importante
    // Utilise UNE SEULE connexion pour tout (atomicité)
    // ─────────────────────────────────────────────────────
    @Override
    public void payerEcheance(Long idEcheance) {
        // Une seule connexion du pool pour toute l'opération
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Marquer l'échéance comme payée
                String sqlPay = "UPDATE Echeance SET statutPaiement='PAYEE' WHERE idEchange=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlPay)) {
                    ps.setLong(1, idEcheance);
                    int rows = ps.executeUpdate();
                    if (rows == 0) {
                        throw new RuntimeException("Aucune échéance trouvée avec l'ID : " + idEcheance);
                    }
                }

                // 2. Trouver l'idContrat de cette échéance
                long idContrat = 0;
                String sqlGetContrat = "SELECT idContrat FROM Echeance WHERE idEchange=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlGetContrat)) {
                    ps.setLong(1, idEcheance);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) idContrat = rs.getLong("idContrat");
                }

                // 3. Vérifier si toutes les échéances du contrat sont payées
                if (idContrat > 0) {
                    String sqlCount = "SELECT COUNT(*) FROM Echeance WHERE idContrat=? AND statutPaiement='IMPAYEE'";
                    try (PreparedStatement ps = conn.prepareStatement(sqlCount)) {
                        ps.setLong(1, idContrat);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            // Toutes payées → clôturer le contrat automatiquement
                            String sqlClose = "UPDATE ContratMourabaha SET statutContrat='CLOTURE' WHERE idContrat=?";
                            try (PreparedStatement ps2 = conn.prepareStatement(sqlClose)) {
                                ps2.setLong(1, idContrat);
                                ps2.executeUpdate();
                                System.out.println("[Contrat #" + idContrat + "] Clôturé automatiquement.");
                            }
                        }
                    }
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Erreur paiement (rollback) : " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion lors du paiement : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // MAPPING ResultSet → Echeance
    // ─────────────────────────────────────────────────────
    private Echeance mapRow(ResultSet rs) throws SQLException {
        Echeance e = new Echeance();
        e.setIdEchange(rs.getLong("idEchange"));
        e.setNumeroEchange(rs.getInt("numeroEchange"));
        e.setDateEchange(rs.getTimestamp("dateEchange").toLocalDateTime());
        e.setMontant(rs.getDouble("montant"));
        e.setStatutPaiement(rs.getString("statutPaiement"));

        // Lier le contrat (agrégation objet)
        ContratMourabaha contrat = new ContratMourabaha();
        contrat.setIdContrat(rs.getLong("idContrat"));
        e.setContrat(contrat);

        return e;
    }
}