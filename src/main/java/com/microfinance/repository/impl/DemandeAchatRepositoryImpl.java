package com.microfinance.repository.impl;

import com.microfinance.model.Client;
import com.microfinance.model.DemandeAchat;
import com.microfinance.repository.DemandeAchatRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeAchatRepositoryImpl implements DemandeAchatRepository {

    @Override
    public DemandeAchat enregistrer(DemandeAchat d) throws SQLException {
        String sql = """
                INSERT INTO demandeachat(datedemande, descriptionbien, prixestime, statutdemande, idclient)
                VALUES (?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(d.getDateDemande()));
            ps.setString(2, d.getDescriptionBien());
            ps.setDouble(3, d.getPrixEstime());
            ps.setString(4, d.getStatutDemande());
            ps.setLong(5, d.getClient().getIdClient());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setIdDemande(keys.getLong(1));
            }
        }
        return d;
    }

    @Override
    public List<DemandeAchat> trouverParClient(Long clientId) throws SQLException {
        List<DemandeAchat> liste = new ArrayList<>();
        String sql = """
                SELECT * FROM demandeachat
                WHERE idclient = ? ORDER BY datedemande DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    @Override
    public List<DemandeAchat> trouverTous() throws SQLException {
        List<DemandeAchat> liste = new ArrayList<>();
        String sql = "SELECT * FROM demandeachat ORDER BY datedemande DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public void changerStatut(Long id, String statut) throws SQLException {
        String sql = "UPDATE demandeachat SET statutdemande = ? WHERE iddemande = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private DemandeAchat mapRow(ResultSet rs) throws SQLException {
        DemandeAchat d = new DemandeAchat();
        d.setIdDemande(rs.getLong("iddemande"));
        d.setDateDemande(rs.getTimestamp("datedemande").toLocalDateTime());
        d.setDescriptionBien(rs.getString("descriptionbien"));
        d.setPrixEstime(rs.getDouble("prixestime"));
        d.setStatutDemande(rs.getString("statutdemande"));
        Client c = new Client();
        c.setIdClient(rs.getLong("idclient"));
        d.setClient(c);
        return d;
    }
}
