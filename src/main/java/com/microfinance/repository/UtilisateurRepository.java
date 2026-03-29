package com.microfinance.repository.impl;

import com.microfinance.model.Utilisateur;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class UtilisateurRepository {

    public Optional<Utilisateur> authentifier(String email, String motDePasse) {

        String sql = "SELECT u.*, c.idClient AS idClientReel " +
                "FROM Utilisateur u " +
                "LEFT JOIN Client c ON LOWER(c.nom) = LOWER(CONCAT(u.nom, ' ', u.prenom)) " +
                "WHERE u.email = ? AND u.motDePasse = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setString(2, motDePasse.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur u = mapRow(rs);
                if ("CLIENT".equals(u.getRole())) {
                    long idClientReel = rs.getLong("idClientReel");
                    if (!rs.wasNull() && idClientReel > 0) {
                        u.setIdUtilisateur(idClientReel);
                    }
                }
                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur authentification : " + e.getMessage(), e);
        }

        String sqlDir = "SELECT idDirecteur AS idUtilisateur, nom, prenom, email, " +
                "telephone, login, motDePasse, role " +
                "FROM Directeur WHERE email = ? AND motDePasse = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlDir)) {

            ps.setString(1, email.trim());
            ps.setString(2, motDePasse.trim());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Utilisateur u = mapRow(rs);
                if (u.getRole() == null || u.getRole().isEmpty()) {
                    u.setRole("DIRECTEUR");
                }
                return Optional.of(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur authentification directeur : " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    public void creer(Utilisateur u) {
        String sql = "INSERT INTO Utilisateur (nom, prenom, email, telephone, login, motDePasse, role) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING idUtilisateur";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getTelephone());
            ps.setString(5, u.getLogin());
            ps.setString(6, u.getMotDePasse());
            ps.setString(7, u.getRole());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                u.setIdUtilisateur(rs.getLong("idUtilisateur"));
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("unique") || e.getMessage().contains("duplicate")) {
                throw new RuntimeException("Cet email est déjà utilisé.", e);
            }
            throw new RuntimeException("Erreur création utilisateur : " + e.getMessage(), e);
        }

        if ("CLIENT".equals(u.getRole())) {
            String sqlClient = "INSERT INTO Client (nom, NNI, adresse, telephone, profession, revenuMensuel, statutClient) " +
                    "VALUES (?, '0000000000', 'Non renseigné', ?, 'Non renseigné', 0, 'ACTIF') RETURNING idClient";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlClient)) {

                ps.setString(1, u.getNom() + " " + u.getPrenom());
                ps.setString(2, u.getTelephone() != null ? u.getTelephone() : "");

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    u.setIdUtilisateur(rs.getLong("idClient"));
                }

            } catch (SQLException e) {
                throw new RuntimeException("Erreur création client : " + e.getMessage(), e);
            }
        }
    }

    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM Utilisateur WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur vérification email : " + e.getMessage(), e);
        }
        return false;
    }

    private Utilisateur mapRow(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setIdUtilisateur(rs.getLong("idUtilisateur"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setTelephone(rs.getString("telephone"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasse(rs.getString("motDePasse"));
        u.setRole(rs.getString("role"));
        return u;
    }
}