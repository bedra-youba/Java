package com.microfinance.repository.impl;

import com.microfinance.model.Fournisseur;
import com.microfinance.repository.FournisseurRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation JDBC de FournisseurRepository.
 *
 * Démontre (cours Java Avancé) :
 *  - Implémentation d'interface (implements)
 *  - JDBC : PreparedStatement, ResultSet, Connection (HikariCP pool)
 *  - Gestion des exceptions (try-with-resources)
 *  - Collections : ArrayList, List
 *  - Encapsulation (getters/setters du modèle)
 *  - Optional<T>
 */
public class FournisseurRepositoryImpl implements FournisseurRepository {

    // ─────────────────────────────────────────────────────
    // CREATE
    // ─────────────────────────────────────────────────────
    @Override
    public void save(Fournisseur fournisseur) {
        String sql = "INSERT INTO Fournisseur (nomEntreprise, telephone, email, adresse) " +
                "VALUES (?, ?, ?, ?) RETURNING idFournisseur";

        // try-with-resources : ferme automatiquement la connexion dans le pool
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fournisseur.getNomEntreprise());
            ps.setString(2, fournisseur.getTelephone());
            ps.setString(3, fournisseur.getEmail());
            ps.setString(4, fournisseur.getAdresse());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                fournisseur.setIdFournisseur(rs.getLong("idFournisseur"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du fournisseur : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // UPDATE
    // ─────────────────────────────────────────────────────
    @Override
    public void update(Fournisseur fournisseur) {
        String sql = "UPDATE Fournisseur SET nomEntreprise=?, telephone=?, email=?, adresse=? " +
                "WHERE idFournisseur=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fournisseur.getNomEntreprise());
            ps.setString(2, fournisseur.getTelephone());
            ps.setString(3, fournisseur.getEmail());
            ps.setString(4, fournisseur.getAdresse());
            ps.setLong(5, fournisseur.getIdFournisseur());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du fournisseur : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM Fournisseur WHERE idFournisseur=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du fournisseur : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────
    // FIND BY ID  — Optional<T>
    // ─────────────────────────────────────────────────────
    @Override
    public Optional<Fournisseur> findById(Long id) {
        String sql = "SELECT * FROM Fournisseur WHERE idFournisseur=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du fournisseur : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────
    // FIND ALL  — Collections : List<Fournisseur>
    // ─────────────────────────────────────────────────────
    @Override
    public List<Fournisseur> findAll() {
        List<Fournisseur> liste = new ArrayList<>();
        String sql = "SELECT * FROM Fournisseur ORDER BY nomEntreprise";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                liste.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des fournisseurs : " + e.getMessage(), e);
        }
        return liste;
    }

    // ─────────────────────────────────────────────────────
    // Mapping ResultSet → Fournisseur (encapsulation)
    // ─────────────────────────────────────────────────────
    private Fournisseur mapRow(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setIdFournisseur(rs.getLong("idFournisseur"));
        f.setNomEntreprise(rs.getString("nomEntreprise"));
        f.setTelephone(rs.getString("telephone"));
        f.setEmail(rs.getString("email"));
        f.setAdresse(rs.getString("adresse"));
        return f;
    }
}