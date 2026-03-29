package com.microfinance.model;

import java.time.LocalDateTime;

/**
 * Modèle DemandeAchat — Flux Mourabaha complet.
 *
 * Statuts :
 *  EN_ATTENTE            → client vient de soumettre
 *  ACCEPTEE              → agent a accepté, doit choisir fournisseur
 *  COMMANDE_FOURNISSEUR  → agent a commandé au fournisseur
 *  BIEN_RECU             → fournisseur a livré, agent a validé
 *  CONTRAT_CREE          → contrat Mourabaha créé, flux terminé
 *  REFUSEE               → agent a refusé
 */
public class DemandeAchat {

    private Long          idDemande;
    private LocalDateTime dateDemande;
    private String        descriptionBien;
    private Double        prixEstime;
    private String        statutDemande;

    // Relation Client
    private Client idClient;

    // Relation Fournisseur — objet complet (pour compatibilité avec FournisseurView)
    private Fournisseur fournisseur;

    // Nom du fournisseur en String — chargé par JOIN SQL
    // ✅ C'est ce champ que lisent toutes les vues via getNomFournisseur()
    private String nomFournisseur;

    // ── Constructeurs ──
    public DemandeAchat() {}

    public DemandeAchat(String descriptionBien, Double prixEstime, Client client) {
        this.descriptionBien = descriptionBien;
        this.prixEstime      = prixEstime;
        this.idClient        = client;
        this.statutDemande   = "EN_ATTENTE";
        this.dateDemande     = LocalDateTime.now();
    }

    // ── Getters / Setters ──
    public Long          getIdDemande()              { return idDemande; }
    public void          setIdDemande(Long v)        { this.idDemande = v; }

    public LocalDateTime getDateDemande()            { return dateDemande; }
    public void          setDateDemande(LocalDateTime v) { this.dateDemande = v; }

    public String getDescriptionBien()               { return descriptionBien; }
    public void   setDescriptionBien(String v)       { this.descriptionBien = v; }

    public Double getPrixEstime()                    { return prixEstime; }
    public void   setPrixEstime(Double v)            { this.prixEstime = v; }

    public String getStatutDemande()                 { return statutDemande; }
    public void   setStatutDemande(String v)         { this.statutDemande = v; }

    public Client getIdClient()                      { return idClient; }
    public void   setIdClient(Client v)              { this.idClient = v; }

    // Objet Fournisseur complet
    public Fournisseur getFournisseur()              { return fournisseur; }
    public void        setFournisseur(Fournisseur v) { this.fournisseur = v; }

    // Nom du fournisseur en String (utilisé dans les TableView des vues)
    public String getNomFournisseur()                { return nomFournisseur; }
    public void   setNomFournisseur(String v)        { this.nomFournisseur = v; }

    @Override
    public String toString() {
        return "DemandeAchat{id=" + idDemande +
                ", bien='" + descriptionBien + "'" +
                ", statut='" + statutDemande + "'" +
                ", fournisseur='" + nomFournisseur + "'}";
    }
}