package com.microfinance.model;

import java.util.ArrayList;
import java.util.List;

public class Bien {

    private Long idBien;
    private String description;
    private double prixAchat;
    private String statutBien;

    private Fournisseur fournisseur;

    // Si un bien est lié à plusieurs contrats (optionnel)
    private List<ContratMourabaha> contrats = new ArrayList<>();

    // Getters et Setters
    public Long getIdBien() { return idBien; }
    public void setIdBien(Long idBien) { this.idBien = idBien; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrixAchat() { return prixAchat; }
    public void setPrixAchat(double prixAchat) { this.prixAchat = prixAchat; }

    public String getStatutBien() { return statutBien; }
    public void setStatutBien(String statutBien) { this.statutBien = statutBien; }

    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }

    public List<ContratMourabaha> getContrats() { return contrats; }
}