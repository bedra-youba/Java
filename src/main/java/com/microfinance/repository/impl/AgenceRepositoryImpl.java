package com.microfinance.repository.impl;



import com.microfinance.model.Agence;
import com.microfinance.repository.AgenceRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class AgenceRepositoryImpl implements AgenceRepository {

    @Override
    public Agence ajouter(Agence a) throws SQLException {
        String sql = """
                INSERT INTO agence(nomagence, ville, telephone, capital)
                VALUES (?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNomAgence());
            ps.setString(2, a.getVille());
            ps.setString(3, a.getTelephone());
            ps.setDouble(4, a.getCapital());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setIdAgence(keys.getLong(1));
            }
        }
        return a;
    }

    @Override
    public Agence trouverParId(Long id) throws SQLException {
        String sql = "SELECT * FROM agence WHERE idagence = ?";
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
    public List<Agence> trouverTous() throws SQLException {
        List<Agence> liste = new ArrayList<>();
        String sql = "SELECT * FROM agence ORDER BY nomagence";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public void modifier(Agence a) throws SQLException {
        String sql = """
                UPDATE agence
                SET nomagence=?, ville=?, telephone=?, capital=?
                WHERE idagence=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNomAgence());
            ps.setString(2, a.getVille());
            ps.setString(3, a.getTelephone());
            ps.setDouble(4, a.getCapital());
            ps.setLong(5, a.getIdAgence());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql = "DELETE FROM agence WHERE idagence = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public double getCapitalTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(capital), 0) FROM agence";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    @Override
    public double getBeneficeParAgence(Long idAgence) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(margebeneficiaire), 0)
                FROM contratmourabaha
                WHERE idagence = ? AND statutcontrat = 'Soldé'
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idAgence);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    private Agence mapRow(ResultSet rs) throws SQLException {
        Agence a = new Agence();
        a.setIdAgence(rs.getLong("idagence"));
        a.setNomAgence(rs.getString("nomagence"));
        a.setVille(rs.getString("ville"));
        a.setTelephone(rs.getString("telephone"));
        a.setCapital(rs.getDouble("capital"));
        return a;
    }
}