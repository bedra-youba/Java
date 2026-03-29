package com.microfinance.repository.impl;

import com.microfinance.model.Client;
import com.microfinance.repository.ClientRepository;
import com.microfinance.Util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientRepositoryImpl implements ClientRepository {

    @Override
    public Client save(Client client) throws SQLException {
        String sql = "INSERT INTO Client (nom, NNI, adresse, telephone, profession, revenuMensuel, statutClient) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING idClient";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getNNI());
            ps.setString(3, client.getAdresse());
            ps.setString(4, client.getTelephone());
            ps.setString(5, client.getProfession());
            ps.setDouble(6, client.getRevenuMensuel());
            ps.setString(7, client.getStatutClient() != null ? client.getStatutClient() : "ACTIF");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                client.setIdClient(rs.getLong(1));
            }
            return client;
        }
    }

    @Override
    public Optional<Client> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM Client WHERE idClient = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        String sql = "SELECT * FROM Client ORDER BY nom";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    @Override
    public void update(Client client) throws SQLException {
        String sql = "UPDATE Client SET nom = ?, NNI = ?, adresse = ?, telephone = ?, " +
                "profession = ?, revenuMensuel = ?, statutClient = ? WHERE idClient = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, client.getNom());
            ps.setString(2, client.getNNI());
            ps.setString(3, client.getAdresse());
            ps.setString(4, client.getTelephone());
            ps.setString(5, client.getProfession());
            ps.setDouble(6, client.getRevenuMensuel());
            ps.setString(7, client.getStatutClient() != null ? client.getStatutClient() : "ACTIF");
            ps.setLong(8, client.getIdClient());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM Client WHERE idClient = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Client> findByNNI(String nni) throws SQLException {
        String sql = "SELECT * FROM Client WHERE NNI = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nni);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Client> rechercherParNom(String nom) throws SQLException {
        String sql = "SELECT * FROM Client WHERE nom ILIKE ?";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + nom + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        }
        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setIdClient(rs.getLong("idClient"));
        client.setNom(rs.getString("nom"));
        client.setNNI(rs.getString("NNI"));
        client.setAdresse(rs.getString("adresse"));
        client.setTelephone(rs.getString("telephone"));
        client.setProfession(rs.getString("profession"));
        client.setRevenuMensuel(rs.getDouble("revenuMensuel"));
        client.setStatutClient(rs.getString("statutClient"));

        // La date d'inscription
        Timestamp ts = rs.getTimestamp("dateInscription");
        if (ts != null) {
            client.setDateInscription(ts.toLocalDateTime());
        }

        return client;
    }
}