package com.microfinance.model;

import java.util.ArrayList;
import java.util.List;

public class Fournisseur {

    private Long idFournisseur;
    private String nomEntreprise;
    private String telephone;
    private String email;
    private String adresse;

    private List<Bien> biens = new ArrayList<>();

    // Getters et Setters
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

    public List<Bien> getBiens() { return biens; }
    public void ajouterBien(Bien b) {
        biens.add(b);
        b.setFournisseur(this);
    }
}