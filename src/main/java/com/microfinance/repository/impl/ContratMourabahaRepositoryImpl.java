package com.microfinance.repository.impl;

import com.microfinance.model.Client;
import com.microfinance.model.ContratMourabaha;
import com.microfinance.repository.ContratMourabahaRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de ContratMourabahaRepository.
 *
 * Démontre (cours Java Avancé) :
 *  - Implémentation d'interface
 *  - JDBC : PreparedStatement, ResultSet, Transactions, Batch
 *  - Gestion des exceptions (try-with-resources)
 *  - Collections (List, ArrayList)
 *  - Agrégation d'objets (ContratMourabaha → Client)
 *  - HikariCP : chaque méthode prend SA propre connexion du pool
 */
public class ContratMourabahaRepositoryImpl implements ContratMourabahaRepository {

    // ─────────────────────────────────────────────────────
    // CREATE — avec transaction JDBC
    // ─────────────────────────────────────────────────────
    @Override
    public void save(ContratMourabaha contrat) {
        String sql = "INSERT INTO ContratMourabaha " +
                "(dateContrat, prixAchatAgence, margeBeneficiaire, prixVenteClient, dureeMois, statutContrat, idClient) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING idContrat";

        // HikariCP : connexion prise du pool, rendue automatiquement après try
        try (Connection conn = DatabaseConnection.getConnection()) {

            // Transaction JDBC (cours JDBC)
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setTimestamp(1, Timestamp.valueOf(
                        contrat.getDateContrat() != null ? contrat.getDateContrat() : LocalDateTime.now()));
                ps.setDouble(2, contrat.getPrixAchatAgence());
                ps.setDouble(3, contrat.getMargeBeneficiaire());
                ps.setDouble(4, contrat.getPrixVenteClient());
                ps.setInt(5, contrat.getDureeMois());
                ps.setString(6, contrat.getStatutContrat() != null ? contrat.getStatutContrat() : "EN_COURS");
                ps.setLong(7, contrat.getClient().getIdClient());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    contrat.setIdContrat(rs.getLong("idContrat"));
                }

                // Générer automatiquement les échéances dans la même transaction
                if (contrat.getDureeMois() != null && contrat.getDureeMois() > 0) {
                    genererEcheances(conn, contrat);
                }

                conn.commit(); // Valider la transaction

            } catch (SQLException e) {
                conn.rollback(); // Annuler si erreur
                throw new RuntimeException("Erreur création contrat (rollback effectué) : " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion lors de la création du contrat : " + e.getMessage(), e);
        }
    }

    /**
     * Génère automatiquement les échéances mensuelles.
     * Même connexion que save() → même transaction.
     * Démontre : boucle, calcul métier, Batch JDBC
     */
    private void genererEcheances(Connection conn, ContratMourabaha contrat) throws SQLException {
        String sqlEch = "INSERT INTO Echeance (numeroEchange, dateEchange, montant, statutPaiement, idContrat) " +
                "VALUES (?, ?, ?, 'IMPAYEE', ?)";

        double montantMensuel = contrat.getPrixVenteClient() / contrat.getDureeMois();
        LocalDateTime dateDepart = LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sqlEch)) {
            for (int i = 1; i <= contrat.getDureeMois(); i++) {
                LocalDateTime dateEcheance = dateDepart.plusMonths(i);
                ps.setInt(1, i);
                ps.setTimestamp(2, Timestamp.valueOf(dateEcheance));
                ps.setDouble(3, montantMensuel);
                ps.setLong(4, contrat.getIdContrat());
                ps.addBatch(); // Batch insert (JDBC avancé)
            }
            ps.executeBatch();
        }
    }

    // ─────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────
    @Override
    public void update(ContratMourabaha contrat) {
        String sql = "UPDATE ContratMourabaha SET " +
                "prixAchatAgence=?, margeBeneficiaire=?, prixVenteClient=?, " +
                "dureeMois=?, statutContrat=? WHERE idContrat=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, contrat.getPrixAchatAgence());
            ps.setDouble(2, contrat.getMargeBeneficiaire());
            ps.setDouble(3, contrat.getPrixVenteClient());
            ps.setInt(4, contrat.getDureeMois());
            ps.setString(5, contrat.getStatutContrat());
            ps.setLong(6, contrat.getIdContrat());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour contrat : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM ContratMourabaha WHERE idContrat=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression contrat : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // FIND BY ID
    // ─────────────────────────────────────────────────────
    @Override
    public Optional<ContratMourabaha> findById(Long id) {
        String sql = "SELECT cm.*, c.nom AS clientNom " +
                "FROM ContratMourabaha cm " +
                "JOIN Client c ON cm.idClient = c.idClient " +
                "WHERE cm.idContrat=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche contrat : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────
    // FIND ALL — Collections
    // ─────────────────────────────────────────────────────
    @Override
    public List<ContratMourabaha> findAll() {
        List<ContratMourabaha> liste = new ArrayList<>();
        String sql = "SELECT cm.*, c.nom AS clientNom " +
                "FROM ContratMourabaha cm " +
                "JOIN Client c ON cm.idClient = c.idClient " +
                "ORDER BY cm.dateContrat DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur récupération contrats : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────
    // FIND BY CLIENT
    // ─────────────────────────────────────────────────────
    @Override
    public List<ContratMourabaha> findByClientId(Long idClient) {
        List<ContratMourabaha> liste = new ArrayList<>();
        String sql = "SELECT cm.*, c.nom AS clientNom " +
                "FROM ContratMourabaha cm " +
                "JOIN Client c ON cm.idClient = c.idClient " +
                "WHERE cm.idClient=? ORDER BY cm.dateContrat DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idClient);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche par client : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────
    // BENEFICE TOTAL — agrégation SQL
    // ─────────────────────────────────────────────────────
    @Override
    public double getBeneficeTotal() {
        String sql = "SELECT COALESCE(SUM(margeBeneficiaire * prixAchatAgence / 100), 0) FROM ContratMourabaha";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur calcul bénéfice : " + e.getMessage(), e);
        }
        return 0.0;
    }

    // ─────────────────────────────────────────────────────
    // MAPPING ResultSet → ContratMourabaha
    // ─────────────────────────────────────────────────────
    private ContratMourabaha mapRow(ResultSet rs) throws SQLException {
        ContratMourabaha c = new ContratMourabaha();
        c.setIdContrat(rs.getLong("idContrat"));
        c.setDateContrat(rs.getTimestamp("dateContrat").toLocalDateTime());
        c.setPrixAchatAgence(rs.getDouble("prixAchatAgence"));
        c.setMargeBeneficiaire(rs.getDouble("margeBeneficiaire"));
        c.setPrixVenteClient(rs.getDouble("prixVenteClient"));
        c.setDureeMois(rs.getInt("dureeMois"));
        c.setStatutContrat(rs.getString("statutContrat"));

        Client client = new Client();
        client.setIdClient(rs.getLong("idClient"));
        client.setNom(rs.getString("clientNom"));
        c.setIdClient(client);

        return c;
    }
}