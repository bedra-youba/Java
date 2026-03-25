
package com.microfinance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Echeance {

    private Long idEchange;
    private int numeroEchange;
    private LocalDateTime dateEchange;
    private double montant;
    private String statutPaiement;

    private ContratMourabaha contrat;
    private List<Paiement> paiements = new ArrayList<>();

    // Getters et Setters
    public Long getIdEchange() { return idEchange; }
    public void setIdEchange(Long idEchange) { this.idEchange = idEchange; }

    public int getNumeroEchange() { return numeroEchange; }
    public void setNumeroEchange(int numeroEchange) { this.numeroEchange = numeroEchange; }

    public LocalDateTime getDateEchange() { return dateEchange; }
    public void setDateEchange(LocalDateTime dateEchange) { this.dateEchange = dateEchange; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(String statutPaiement) { this.statutPaiement = statutPaiement; }

    public ContratMourabaha getContrat() { return contrat; }
    public void setContrat(ContratMourabaha contrat) { this.contrat = contrat; }

    public List<Paiement> getPaiements() { return paiements; }
    public void ajouterPaiement(Paiement p) {
        paiements.add(p);
        p.setEcheance(this);
    }
}