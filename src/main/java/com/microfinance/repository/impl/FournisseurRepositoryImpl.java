

package com.microfinance.repository.impl;

import com.microfinance.model.Fournisseur;
import com.microfinance.repository.FournisseurRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class FournisseurRepositoryImpl implements FournisseurRepository {

    @Override
    public Fournisseur ajouter(Fournisseur f) throws SQLException {
        String sql = """
                INSERT INTO fournisseur(nomentreprise, telephone, email, adresse)
                VALUES (?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNomEntreprise());
            ps.setString(2, f.getTelephone());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) f.setIdFournisseur(keys.getLong(1));
            }
        }
        return f;
    }

    @Override
    public List<Fournisseur> trouverTous() throws SQLException {
        List<Fournisseur> liste = new ArrayList<>();
        String sql = "SELECT * FROM fournisseur ORDER BY nomentreprise";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public Fournisseur trouverParId(Long id) throws SQLException {
        String sql = "SELECT * FROM fournisseur WHERE idfournisseur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    @Override
    public void modifier(Fournisseur f) throws SQLException {
        String sql = """
                UPDATE fournisseur
                SET nomentreprise=?, telephone=?, email=?, adresse=?
                WHERE idfournisseur=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getNomEntreprise());
            ps.setString(2, f.getTelephone());
            ps.setString(3, f.getEmail());
            ps.setString(4, f.getAdresse());
            ps.setLong(5, f.getIdFournisseur());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql = "DELETE FROM fournisseur WHERE idfournisseur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Fournisseur mapRow(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setIdFournisseur(rs.getLong("idfournisseur"));
        f.setNomEntreprise(rs.getString("nomentreprise"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        return f;
    }
}