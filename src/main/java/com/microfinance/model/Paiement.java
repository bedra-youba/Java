package com.microfinance.model;

import java.time.LocalDateTime;

public class Paiement {
    private Long idPaiement;
    private LocalDateTime datePaiement;
    private Double montantPaye;
    private String modePaiement;
    private Echeance echeance;  // Relation vers idEcheance

    // Constructeurs
    public Paiement() {}

    public Paiement(Double montantPaye, String modePaiement, Echeance echeance) {
        this.datePaiement = LocalDateTime.now();
        this.montantPaye = montantPaye;
        this.modePaiement = modePaiement;
        this.echeance = echeance;
    }

    // Getters/Setters
    public Long getIdPaiement() { return idPaiement; }
    public void setIdPaiement(Long idPaiement) { this.idPaiement = idPaiement; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public Double getMontantPaye() { return montantPaye; }
    public void setMontantPaye(Double montantPaye) { this.montantPaye = montantPaye; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public Echeance getEcheance() { return echeance; }
    public void setEcheance(Echeance echeance) { this.echeance = echeance; }
}