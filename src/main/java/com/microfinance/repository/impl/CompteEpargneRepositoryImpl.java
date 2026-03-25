package com.microfinance.repository.impl;



import com.microfinance.model.Client;
import com.microfinance.model.CompteEpargne;
import com.microfinance.repository.CompteEpargneRepository;
import com.microfinance.Util.DatabaseConnection;

import java.sql.*;
        import java.util.ArrayList;
import java.util.List;

public class CompteEpargneRepositoryImpl implements CompteEpargneRepository {

    @Override
    public CompteEpargne creer(CompteEpargne cp) throws SQLException {
        String sql = """
                INSERT INTO compteepargne(solde, datecreation, statut, idclient)
                VALUES (?,?,?,?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, cp.getSolde());
            ps.setTimestamp(2, Timestamp.valueOf(cp.getDateCreation()));
            ps.setString(3, cp.getStatut());
            ps.setLong(4, cp.getClient().getIdClient());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) cp.setIdCompte(keys.getLong(1));
            }
        }
        return cp;
    }

    @Override
    public CompteEpargne trouverParId(Long id) throws SQLException {
        String sql = "SELECT * FROM compteepargne WHERE idcompte = ?";
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
    public List<CompteEpargne> trouverParClient(Long clientId) throws SQLException {
        List<CompteEpargne> liste = new ArrayList<>();
        String sql = "SELECT * FROM compteepargne WHERE idclient = ?";
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
    public void deposer(Long idCompte, double montant) throws SQLException {
        String sql = "UPDATE compteepargne SET solde = solde + ? WHERE idcompte = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, montant);
            ps.setLong(2, idCompte);
            ps.executeUpdate();
        }
    }

    @Override
    public void retirer(Long idCompte, double montant) throws SQLException {
        double solde = getSolde(idCompte);
        if (solde < montant)
            throw new SQLException(
                    "Solde insuffisant : " + solde + " DA disponible"
            );
        String sql = "UPDATE compteepargne SET solde = solde - ? WHERE idcompte = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, montant);
            ps.setLong(2, idCompte);
            ps.executeUpdate();
        }
    }

    @Override
    public double getSolde(Long idCompte) throws SQLException {
        String sql = "SELECT solde FROM compteepargne WHERE idcompte = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idCompte);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("solde");
            }
        }
        return 0;
    }

    private CompteEpargne mapRow(ResultSet rs) throws SQLException {
        CompteEpargne cp = new CompteEpargne();
        cp.setIdCompte(rs.getLong("idcompte"));
        cp.setSolde(rs.getDouble("solde"));
        cp.setDateCreation(rs.getTimestamp("datecreation").toLocalDateTime());
        cp.setStatut(rs.getString("statut"));
        Client c = new Client();
        c.setIdClient(rs.getLong("idclient"));
        cp.setClient(c);
        return cp;
    }
}