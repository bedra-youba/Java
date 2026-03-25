//package com.microfinance.model;
//
//public class Client {
//    private int id;
//    private String nom;
//    private String telephone;
//    private String adresse;
//    private int scoreFiabilite;
//    private int agenceId; // <-- Vérifie que cette ligne existe
//
//    // Vérifie que ton constructeur prend bien l'agenceId
//    public Client(String nom, String telephone, String adresse, int scoreFiabilite, int agenceId) {
//        this.nom = nom;
//        this.telephone = telephone;
//        this.adresse = adresse;
//        this.scoreFiabilite = scoreFiabilite;
//        this.agenceId = agenceId;
//    }
//
//    // AJOUTE CE GETTER SI IL MANQUE :
//    public int getAgenceId() {
//        return agenceId;
//    }
//
//    // Les autres getters classiques
//    public String getNom() { return nom; }
//    public String getTelephone() { return telephone; }
//    public String getAdresse() { return adresse; }
//    public int getScoreFiabilite() { return scoreFiabilite; }
//}



package com.microfinance.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Client {

    private Long idClient;
    private String nom;
    private String NNI;
    private String adresse;
    private String telephone;
    private String profession;
    private double revenuMensuel;
    private LocalDateTime dateInscription;
    private String statutClient;

    private List<DemandeAchat> demandes = new ArrayList<>();
    private List<ContratMourabaha> contrats = new ArrayList<>();
    private List<CompteEpargne> comptesEpargne = new ArrayList<>();

    // Getters et Setters
    public Long getIdClient() { return idClient; }
    public void setIdClient(Long idClient) { this.idClient = idClient; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getNNI() { return NNI; }
    public void setNNI(String NNI) { this.NNI = NNI; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    public double getRevenuMensuel() { return revenuMensuel; }
    public void setRevenuMensuel(double revenuMensuel) { this.revenuMensuel = revenuMensuel; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public String getStatutClient() { return statutClient; }
    public void setStatutClient(String statutClient) { this.statutClient = statutClient; }

    public List<DemandeAchat> getDemandes() { return demandes; }
    public List<ContratMourabaha> getContrats() { return contrats; }
    public List<CompteEpargne> getComptesEpargne() { return comptesEpargne; }

    // Méthodes pour maintenir la bidirectionnalité
    public void ajouterDemande(DemandeAchat demande) {
        demandes.add(demande);
        demande.setClient(this);
    }

    public void ajouterContrat(ContratMourabaha contrat) {
        contrats.add(contrat);
        contrat.setClient(this);
    }

    public void ajouterCompteEpargne(CompteEpargne compte) {
        comptesEpargne.add(compte);
        compte.setClient(this);
    }
}
