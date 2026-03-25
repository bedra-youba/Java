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
    public Client ajouter(Client c) throws SQLException {
        String sql = """
                INSERT INTO client(nom, nni, adresse, telephone, profession,
                revenumensuel, dateinscription, statutclient)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNNI());
            ps.setString(3, c.getAdresse());
            ps.setString(4, c.getTelephone());
            ps.setString(5, c.getProfession());
            ps.setDouble(6, c.getRevenuMensuel());
            ps.setTimestamp(7, Timestamp.valueOf(c.getDateInscription()));
            ps.setString(8, c.getStatutClient());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setIdClient(keys.getLong(1));
            }
        }
        return c;
    }

    @Override
    public Optional<Client> trouverParId(Long id) throws SQLException {
        String sql = "SELECT * FROM client WHERE idclient = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Client> trouverParNNI(String nni) throws SQLException {
        String sql = "SELECT * FROM client WHERE nni = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Client> trouverTous() throws SQLException {
        List<Client> liste = new ArrayList<>();
        String sql = "SELECT * FROM client ORDER BY nom";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public void modifier(Client c) throws SQLException {
        String sql = """
                UPDATE client SET nom=?, nni=?, adresse=?, telephone=?,
                profession=?, revenumensuel=?, statutclient=?
                WHERE idclient=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getNNI());
            ps.setString(3, c.getAdresse());
            ps.setString(4, c.getTelephone());
            ps.setString(5, c.getProfession());
            ps.setDouble(6, c.getRevenuMensuel());
            ps.setString(7, c.getStatutClient());
            ps.setLong(8, c.getIdClient());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql = "DELETE FROM client WHERE idclient = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int getNombreClients() throws SQLException {
        String sql = "SELECT COUNT(*) FROM client";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setIdClient(rs.getLong("idclient"));
        c.setNom(rs.getString("nom"));
        c.setNNI(rs.getString("nni"));
        c.setAdresse(rs.getString("adresse"));
        c.setTelephone(rs.getString("telephone"));
        c.setProfession(rs.getString("profession"));
        c.setRevenuMensuel(rs.getDouble("revenumensuel"));
        c.setDateInscription(rs.getTimestamp("dateinscription").toLocalDateTime());
        c.setStatutClient(rs.getString("statutclient"));
        return c;
    }
}