
package com.microfinance.model;

import java.time.LocalDate;

public class Agent extends Utilisateur {

    private String matricule;
    private String poste;
    private LocalDate dateEmbauche;

    // Méthodes métier
    public void ajouterClient() {}
    public void modifierClient() {}
    public void creerCompteEpargne() {}
    public void enregistrerDepot() {}
    public void enregistrerRetrait() {}
    public void enregistrerDemandeAchat() {}
    public void commanderFourniture() {}
    public void enregistrerAchatBien() {}
    public void creerContratMourabaha() {}
    public void validerLivraisonBien() {}
    public void enregistrerPaiementEcheance() {}

    // Getters et Setters
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }
}