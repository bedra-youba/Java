package com.microfinance.model;

import java.time.LocalDateTime;

public class CompteEpargne {

    private Long idCompte;
    private double solde;
    private LocalDateTime dateCreation;
    private String statut;

    private Client client;

    // Getters et Setters
    public Long getIdCompte() { return idCompte; }
    public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }

    public double getSolde() { return solde; }
    public void setSolde(double solde) { this.solde = solde; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}