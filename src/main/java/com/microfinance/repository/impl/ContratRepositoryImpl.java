
// repository/impl/ContratRepositoryImpl.java
package com.microfinance.repository.impl;

import com.microfinance.model.Client;
import com.microfinance.model.ContratMourabaha;
import com.microfinance.repository.ContratRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContratRepositoryImpl implements ContratRepository {

    @Override
    public ContratMourabaha ajouter(ContratMourabaha c) throws SQLException {
        String sql = """
                INSERT INTO contratmourabaha
                (datecontrat, prixachatagence, margebeneficiaire, prixventeclient,
                 dureemois, statutcontrat, idclient)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(c.getDateContrat()));
            ps.setDouble(2, c.getPrixAchatAgence());
            ps.setDouble(3, c.getMargeBeneficiaire());
            ps.setDouble(4, c.getPrixVenteClient());
            ps.setInt(5, c.getDureeMois());
            ps.setString(6, c.getStatutContrat());
            ps.setLong(7, c.getClient().getIdClient());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setIdContrat(keys.getLong(1));
            }
        }
        return c;
    }

    @Override
    public Optional<ContratMourabaha> trouverParId(Long id) throws SQLException {
        String sql = "SELECT * FROM contratmourabaha WHERE idcontrat = ?";
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
    public List<ContratMourabaha> trouverTous() throws SQLException {
        List<ContratMourabaha> liste = new ArrayList<>();
        String sql = "SELECT * FROM contratmourabaha ORDER BY datecontrat DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    @Override
    public List<ContratMourabaha> trouverParClient(Long clientId) throws SQLException {
        List<ContratMourabaha> liste = new ArrayList<>();
        String sql = "SELECT * FROM contratmourabaha WHERE idclient = ?";
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
    public void modifier(ContratMourabaha c) throws SQLException {
        String sql = """
                UPDATE contratmourabaha
                SET statutcontrat=?, margebeneficiaire=?, prixventeclient=?, dureemois=?
                WHERE idcontrat=?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getStatutContrat());
            ps.setDouble(2, c.getMargeBeneficiaire());
            ps.setDouble(3, c.getPrixVenteClient());
            ps.setInt(4, c.getDureeMois());
            ps.setLong(5, c.getIdContrat());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(Long id) throws SQLException {
        String sql = "DELETE FROM contratmourabaha WHERE idcontrat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public int getNombreContrats() throws SQLException {
        String sql = "SELECT COUNT(*) FROM contratmourabaha";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private ContratMourabaha mapRow(ResultSet rs) throws SQLException {
        ContratMourabaha c = new ContratMourabaha();
        c.setIdContrat(rs.getLong("idcontrat"));
        c.setDateContrat(rs.getTimestamp("datecontrat").toLocalDateTime());
        c.setPrixAchatAgence(rs.getDouble("prixachatagence"));
        c.setMargeBeneficiaire(rs.getDouble("margebeneficiaire"));
        c.setPrixVenteClient(rs.getDouble("prixventeclient"));
        c.setDureeMois(rs.getInt("dureemois"));
        c.setStatutContrat(rs.getString("statutcontrat"));
        Client client = new Client();
        client.setIdClient(rs.getLong("idclient"));
        c.setClient(client);
        return c;
    }
}
