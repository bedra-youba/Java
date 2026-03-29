package com.microfinance.model;

public class Agence {

    private Long idAgence;
    private String nomAgence;
    private String ville;
    private String telephone;
    private double capital;

    // Getters et Setters
    public Long getIdAgence() { return idAgence; }
    public void setIdAgence(Long idAgence) { this.idAgence = idAgence; }

    public String getNomAgence() { return nomAgence; }
    public void setNomAgence(String nomAgence) { this.nomAgence = nomAgence; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public double getCapital() { return capital; }
    public void setCapital(double capital) { this.capital = capital; }
}