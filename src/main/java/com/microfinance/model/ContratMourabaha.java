
 package com.microfinance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContratMourabaha {

    private Long idContrat;
    private LocalDateTime dateContrat;
    private double prixAchatAgence;
    private double margeBeneficiaire;
    private double prixVenteClient;
    private int dureeMois;
    private String statutContrat;

    private Client client;
    private List<Echeance> echeances = new ArrayList<>();

    // Getters et Setters
    public Long getIdContrat() { return idContrat; }
    public void setIdContrat(Long idContrat) { this.idContrat = idContrat; }

    public LocalDateTime getDateContrat() { return dateContrat; }
    public void setDateContrat(LocalDateTime dateContrat) { this.dateContrat = dateContrat; }

    public double getPrixAchatAgence() { return prixAchatAgence; }
    public void setPrixAchatAgence(double prixAchatAgence) { this.prixAchatAgence = prixAchatAgence; }

    public double getMargeBeneficiaire() { return margeBeneficiaire; }
    public void setMargeBeneficiaire(double margeBeneficiaire) { this.margeBeneficiaire = margeBeneficiaire; }

    public double getPrixVenteClient() { return prixVenteClient; }
    public void setPrixVenteClient(double prixVenteClient) { this.prixVenteClient = prixVenteClient; }

    public int getDureeMois() { return dureeMois; }
    public void setDureeMois(int dureeMois) { this.dureeMois = dureeMois; }

    public String getStatutContrat() { return statutContrat; }
    public void setStatutContrat(String statutContrat) { this.statutContrat = statutContrat; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public List<Echeance> getEcheances() { return echeances; }

    public void ajouterEcheance(Echeance e) {
        echeances.add(e);
        e.setContrat(this);
    }
}
