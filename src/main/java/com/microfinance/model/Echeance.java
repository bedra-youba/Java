package com.microfinance.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Echeance {
    private Long idEchange;           // Correspond à idEchange
    private Integer numeroEchange;     // numeroEchange
    private LocalDateTime dateEchange; // dateEchange
    private Double montant;            // montant
    private String statutPaiement;     // statutPaiement
    private LocalDateTime datePaiement; // ← AJOUTEZ CETTE LIGNE

    // Relation Many-to-One
    private ContratMourabaha contrat;  // idContrat

    public Echeance() {}

    public Echeance(Integer numeroEchange, Double montant, LocalDateTime dateEchange, ContratMourabaha contrat) {
        this.numeroEchange = numeroEchange;
        this.montant = montant;
        this.dateEchange = dateEchange;
        this.statutPaiement = "IMPAYEE";
        this.contrat = contrat;
    }

    // Getters et Setters avec vos noms exacts
    public Long getIdEchange() { return idEchange; }
    public void setIdEchange(Long idEchange) { this.idEchange = idEchange; }

    public Integer getNumeroEchange() { return numeroEchange; }
    public void setNumeroEchange(Integer numeroEchange) { this.numeroEchange = numeroEchange; }

    public LocalDateTime getDateEchange() { return dateEchange; }
    public void setDateEchange(LocalDateTime dateEchange) { this.dateEchange = dateEchange; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    // ✅ AJOUTER CES GETTER ET SETTER
    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public ContratMourabaha getContrat() { return contrat; }
    public void setContrat(ContratMourabaha contrat) { this.contrat = contrat; }

    // Méthodes métier
    public void payer() {
        this.statutPaiement = "PAYEE";
        this.datePaiement = LocalDateTime.now(); // ← MODIFIER AUSSI CETTE MÉTHODE
    }

    public boolean estEnRetard() {
        return LocalDateTime.now().isAfter(dateEchange) && "IMPAYEE".equals(statutPaiement);
    }

    @Override
    public String toString() {
        return "Echeance{" +
                "n°" + numeroEchange +
                ", montant=" + montant +
                ", statut=" + statutPaiement +
                '}';
    }
}