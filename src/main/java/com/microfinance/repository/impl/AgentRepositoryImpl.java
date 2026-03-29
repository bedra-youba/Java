package com.microfinance.repository.impl;



import com.microfinance.model.Agent;
import com.microfinance.repository.AgentRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgentRepositoryImpl implements AgentRepository {

    @Override
    public Agent ajouter(Agent a) throws SQLException {
        String sqlUtil = """
                INSERT INTO utilisateur(nom, prenom, email, telephone, login, motdepasse, role)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUtil,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getTelephone());
            ps.setString(5, a.getLogin());
            ps.setString(6, a.getMotDePasse());
            ps.setString(7, a.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.setIdUtilisateur(keys.getLong(1));
            }
        }

        String sqlAgent = """
                INSERT INTO agent(idutilisateur, matricule, poste, dateembauche)
                VALUES (?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlAgent)) {
            ps.setLong(1, a.getIdUtilisateur());
            ps.setString(2, a.getMatricule());
            ps.setString(3, a.getPoste());
            ps.setDate(4, Date.valueOf(a.getDateEmbauche()));
            ps.executeUpdate();
        }
        return a;
    }

    @Override
    public Agent trouverParId(Long id) throws SQLException {
        String sql = """
                SELECT u.*, a.matricule, a.poste, a.dateembauche
                FROM utilisateur u
                JOIN agent a ON u.idutilisateur = a.idutilisateur
                WHERE u.idutilisateur = ?
                """;
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
    public List<Agent> trouverTous() throws SQLException {
        List<Agent> liste = new ArrayList<>();
        String sql = """
                SELECT u.*, a.matricule, a.poste, a.dateembauche
                FROM utilisateur u
                JOIN agent a ON u.idutilisateur = a.idutilisateur
                ORDER BY u.nom
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public void modifier(Agent a) throws SQLException {
        String sql = """
                UPDATE utilisateur
                SET nom=?, prenom=?, email=?, telephone=?
                WHERE idutilisateur=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNom());
            ps.setString(2, a.getPrenom());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getTelephone());
            ps.setLong(5, a.getIdUtilisateur());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql1 = "DELETE FROM agent WHERE idutilisateur = ?";
        String sql2 = "DELETE FROM utilisateur WHERE idutilisateur = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps1.setLong(1, id);
                ps1.executeUpdate();
                ps2.setLong(1, id);
                ps2.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public boolean existeMatricule(String matricule) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    @Override
    public int getNombreAgents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM agent";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Agent mapRow(ResultSet rs) throws SQLException {
        Agent a = new Agent();
        a.setIdUtilisateur(rs.getLong("idutilisateur"));
        a.setNom(rs.getString("nom"));
        a.setPrenom(rs.getString("prenom"));
        a.setEmail(rs.getString("email"));
        a.setTelephone(rs.getString("telephone"));
        a.setLogin(rs.getString("login"));
        a.setMotDePasse(rs.getString("motdepasse"));
        a.setRole(rs.getString("role"));
        a.setMatricule(rs.getString("matricule"));
        a.setPoste(rs.getString("poste"));
        a.setDateEmbauche(rs.getDate("dateembauche").toLocalDate());
        return a;
    }
}
