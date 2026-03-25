

// repository/impl/EcheanceRepositoryImpl.java
package com.microfinance.repository.impl;

import com.microfinance.model.Echeance;
import com.microfinance.repository.EcheanceRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class EcheanceRepositoryImpl implements EcheanceRepository {

    @Override
    public Echeance ajouter(Echeance e) throws SQLException {
        String sql = """
                INSERT INTO echeance(numeroechange, dateechange, montant, statutpaiement, idcontrat)
                VALUES (?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getNumeroEchange());
            ps.setTimestamp(2, Timestamp.valueOf(e.getDateEchange()));
            ps.setDouble(3, e.getMontant());
            ps.setString(4, e.getStatutPaiement());
            ps.setLong(5, e.getContrat().getIdContrat());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setIdEchange(keys.getLong(1));
            }
        }
        return e;
    }

    @Override
    public List<Echeance> trouverParContrat(Long contratId) throws SQLException {
        List<Echeance> liste = new ArrayList<>();
        String sql = "SELECT * FROM echeance WHERE idcontrat = ? ORDER BY numeroechange";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, contratId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    @Override
    public void marquerPayee(Long idEchange) throws SQLException {
        String sql = "UPDATE echeance SET statutpaiement='PAYE' WHERE idechange = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idEchange);
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql = "DELETE FROM echeance WHERE idechange = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Echeance mapRow(ResultSet rs) throws SQLException {
        Echeance e = new Echeance();
        e.setIdEchange(rs.getLong("idechange"));
        e.setNumeroEchange(rs.getInt("numeroechange"));
        e.setDateEchange(rs.getTimestamp("dateechange").toLocalDateTime());
        e.setMontant(rs.getDouble("montant"));
        e.setStatutPaiement(rs.getString("statutpaiement"));
        return e;
    }
}