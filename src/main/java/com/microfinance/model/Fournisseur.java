package com.microfinance.model;

import java.util.Objects;

public class Fournisseur {
    private Long idFournisseur;
    private String nomEntreprise;
    private String telephone;
    private String email;
    private String adresse;

    // Constructeur par défaut
    public Fournisseur() {}

    // Constructeur avec paramètres
    public Fournisseur(String nomEntreprise, String telephone, String email, String adresse) {
        this.nomEntreprise = nomEntreprise;
        this.telephone = telephone;
        this.email = email;
        this.adresse = adresse;
    }

    // Getters et Setters (encapsulation - page 102)
    public Long getIdFournisseur() { return idFournisseur; }
    public void setIdFournisseur(Long idFournisseur) { this.idFournisseur = idFournisseur; }

    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    // toString() pour débogage (page 103)
    @Override
    public String toString() {
        return "Fournisseur{" +
                "id=" + idFournisseur +
                ", entreprise='" + nomEntreprise + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // equals et hashCode (page 103)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return Objects.equals(idFournisseur, that.idFournisseur);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFournisseur);
    }
}