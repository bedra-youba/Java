package com.microfinance.repository;


import com.microfinance.model.Agence;
import java.sql.SQLException;
import java.util.List;

public interface AgenceRepository {
    Agence ajouter(Agence agence) throws SQLException;
    Agence trouverParId(Long id) throws SQLException;
    List<Agence> trouverTous() throws SQLException;
    void modifier(Agence agence) throws SQLException;
    void supprimer(Long id) throws SQLException;
    double getCapitalTotal() throws SQLException;
    double getBeneficeParAgence(Long idAgence) throws SQLException;
}