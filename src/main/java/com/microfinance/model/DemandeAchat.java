package com.microfinance.model;

import java.time.LocalDateTime;

public class DemandeAchat {

    private Long idDemande;
    private LocalDateTime dateDemande;
    private String descriptionBien;
    private double prixEstime;
    private String statutDemande;

    private Client client;

    // Getters et Setters
    public Long getIdDemande() { return idDemande; }
    public void setIdDemande(Long idDemande) { this.idDemande = idDemande; }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) { this.dateDemande = dateDemande; }

    public String getDescriptionBien() { return descriptionBien; }
    public void setDescriptionBien(String descriptionBien) { this.descriptionBien = descriptionBien; }

    public double getPrixEstime() { return prixEstime; }
    public void setPrixEstime(double prixEstime) { this.prixEstime = prixEstime; }

    public String getStatutDemande() { return statutDemande; }
    public void setStatutDemande(String statutDemande) { this.statutDemande = statutDemande; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
}