package com.microfinance.repository.impl;

import com.microfinance.model.Client;
import com.microfinance.model.DemandeAchat;
import com.microfinance.model.Fournisseur;
import com.microfinance.repository.DemandeAchatRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandeAchatRepositoryImpl implements DemandeAchatRepository {

    @Override
    public void save(DemandeAchat demande) {
        String sql = "INSERT INTO DemandeAchat (dateDemande, descriptionBien, prixEstime, statutDemande, idClient) " +
                "VALUES (NOW(), ?, ?, ?, ?) RETURNING idDemande";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, demande.getDescriptionBien());
            if (demande.getPrixEstime() != null && demande.getPrixEstime() > 0)
                ps.setDouble(2, demande.getPrixEstime());
            else
                ps.setNull(2, Types.NUMERIC);
            ps.setString(3, demande.getStatutDemande() != null ? demande.getStatutDemande() : "EN_ATTENTE");
            ps.setLong(4, demande.getIdClient().getIdClient());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) demande.setIdDemande(rs.getLong("idDemande"));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur sauvegarde demande : " + e.getMessage(), e);
        }
    }

    @Override
    public void updateStatut(Long idDemande, String nouveauStatut) {
        String sql = "UPDATE DemandeAchat SET statutDemande = ? WHERE idDemande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setLong(2, idDemande);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour statut : " + e.getMessage(), e);
        }
    }

    @Override
    public void updateFournisseurEtStatut(Long idDemande, Long idFournisseur, String nouveauStatut) {
        String sql = "UPDATE DemandeAchat SET statutDemande = ?, idFournisseur = ? WHERE idDemande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nouveauStatut);
            ps.setLong(2, idFournisseur);
            ps.setLong(3, idDemande);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise à jour fournisseur/statut : " + e.getMessage(), e);
        }
    }

    public void updateContratEtStatut(Long idDemande, Long idContrat) {
        String sql = "UPDATE DemandeAchat SET statutDemande = 'CONTRAT_CREE' WHERE idDemande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idDemande);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur liaison contrat : " + e.getMessage(), e);
        }
    }

    @Override
    public List<DemandeAchat> findByClientId(Long idClient) {
        List<DemandeAchat> liste = new ArrayList<>();
        String sql = "SELECT d.*, c.nom AS clientNom, f.nomEntreprise AS fournisseurNom " +
                "FROM DemandeAchat d " +
                "LEFT JOIN Client c ON d.idClient = c.idClient " +
                "LEFT JOIN Fournisseur f ON d.idFournisseur = f.idFournisseur " +
                "WHERE d.idClient = ? ORDER BY d.dateDemande DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idClient);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur chargement demandes client : " + e.getMessage(), e);
        }
        return liste;
    }

    @Override
    public List<DemandeAchat> findAll() {
        List<DemandeAchat> liste = new ArrayList<>();
        String sql = "SELECT d.*, c.nom AS clientNom, f.nomEntreprise AS fournisseurNom " +
                "FROM DemandeAchat d " +
                "LEFT JOIN Client c ON d.idClient = c.idClient " +
                "LEFT JOIN Fournisseur f ON d.idFournisseur = f.idFournisseur " +
                "ORDER BY d.dateDemande DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur chargement toutes demandes : " + e.getMessage(), e);
        }
        return liste;
    }

    @Override
    public Optional<DemandeAchat> findById(Long id) {
        String sql = "SELECT d.*, c.nom AS clientNom, f.nomEntreprise AS fournisseurNom " +
                "FROM DemandeAchat d " +
                "LEFT JOIN Client c ON d.idClient = c.idClient " +
                "LEFT JOIN Fournisseur f ON d.idFournisseur = f.idFournisseur " +
                "WHERE d.idDemande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche demande : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private DemandeAchat mapRow(ResultSet rs) throws SQLException {
        DemandeAchat d = new DemandeAchat();
        d.setIdDemande(rs.getLong("idDemande"));
        d.setDescriptionBien(rs.getString("descriptionBien"));

        double prix = rs.getDouble("prixEstime");
        if (!rs.wasNull()) d.setPrixEstime(prix);

        d.setStatutDemande(rs.getString("statutDemande"));

        Timestamp ts = rs.getTimestamp("dateDemande");
        if (ts != null) d.setDateDemande(ts.toLocalDateTime());

        // Client
        Client client = new Client();
        client.setIdClient(rs.getLong("idClient"));
        try {
            String nom = rs.getString("clientNom");
            if (nom != null) client.setNom(nom);
        } catch (Exception ignored) {}
        d.setIdClient(client);

        // Fournisseur
        try {
            long idF = rs.getLong("idFournisseur");
            if (!rs.wasNull()) {
                Fournisseur f = new Fournisseur();
                f.setIdFournisseur(idF);
                String nomF = rs.getString("fournisseurNom");
                if (nomF != null) {
                    f.setNomEntreprise(nomF);
                    // ✅ FIX : sans cette ligne, getNomFournisseur() retourne null
                    // dans DemandeAgentView, FournisseurView et DashboardClientView
                    d.setNomFournisseur(nomF);
                }
                d.setFournisseur(f);
            }
        } catch (Exception ignored) {}

        return d;
    }
}