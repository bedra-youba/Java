package com.microfinance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContratMourabaha {
    private Long idContrat;                 // idContrat
    private LocalDateTime dateContrat;       // dateContrat
    private Double prixAchatAgence;          // prixAchatAgence
    private Double margeBeneficiaire;        // margeBeneficiaire
    private Double prixVenteClient;          // prixVenteClient
    private Integer dureeMois;                // dureeMois
    private String statutContrat;             // statutContrat

    // Relations
    private Client client;                    // idClient
    private Fournisseur fournisseur;          // idFournisseur (ajouté)
    private List<Echeance> echeances = new ArrayList<>();

    // Constructeurs
    public ContratMourabaha() {}

    public ContratMourabaha(Double prixAchatAgence, Double margeBeneficiaire,
                            Integer dureeMois, Client client, Fournisseur fournisseur) {
        this.dateContrat = LocalDateTime.now();
        this.prixAchatAgence = prixAchatAgence;
        this.margeBeneficiaire = margeBeneficiaire;
        this.prixVenteClient = prixAchatAgence + (prixAchatAgence * margeBeneficiaire / 100);
        this.dureeMois = dureeMois;
        this.statutContrat = "EN_COURS";
        this.client = client;
        this.fournisseur = fournisseur;
    }

    // Getters et Setters avec vos noms exacts
    public Long getIdContrat() { return idContrat; }
    public void setIdContrat(Long idContrat) { this.idContrat = idContrat; }

    public LocalDateTime getDateContrat() { return dateContrat; }
    public void setDateContrat(LocalDateTime dateContrat) { this.dateContrat = dateContrat; }

    public Double getPrixAchatAgence() { return prixAchatAgence; }
    public void setPrixAchatAgence(Double prixAchatAgence) {
        this.prixAchatAgence = prixAchatAgence;
        recalculerPrixVente();
    }

    public Double getMargeBeneficiaire() { return margeBeneficiaire; }
    public void setMargeBeneficiaire(Double margeBeneficiaire) {
        this.margeBeneficiaire = margeBeneficiaire;
        recalculerPrixVente();
    }

    public Double getPrixVenteClient() { return prixVenteClient; }
    public void setPrixVenteClient(Double prixVenteClient) { this.prixVenteClient = prixVenteClient; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public String getStatutContrat() { return statutContrat; }
    public void setStatutContrat(String statutContrat) { this.statutContrat = statutContrat; }

    public Client getClient() { return client; }
    public void setIdClient(Client client) { this.client = client; }

    public Fournisseur getFournisseur() { return fournisseur; }
    public void setFournisseur(Fournisseur fournisseur) { this.fournisseur = fournisseur; }

    public List<Echeance> getEcheances() { return echeances; }
    public void setEcheances(List<Echeance> echeances) { this.echeances = echeances; }

    private void recalculerPrixVente() {
        if (prixAchatAgence != null && margeBeneficiaire != null) {
            this.prixVenteClient = prixAchatAgence + (prixAchatAgence * margeBeneficiaire / 100);
        }
    }

    public void addEcheance(Echeance echeance) {
        this.echeances.add(echeance);
        echeance.setContrat(this);
    }

    @Override
    public String toString() {
        return "ContratMourabaha{" +
                "id=" + idContrat +
                ", client=" + (client != null ? client.getNom() : "null") +
                ", montant=" + prixVenteClient +
                ", statut=" + statutContrat +
                '}';
    }
}