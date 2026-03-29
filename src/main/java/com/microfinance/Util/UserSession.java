package com.microfinance.Util;

/**
 * Session utilisateur — Singleton partagé entre toutes les vues.
 * Stocke l'utilisateur connecté et son rôle pour le RBAC.
 *
 * Démontre : Singleton, encapsulation, gestion de session
 */
public class UserSession {

    private static UserSession instance;

    // Infos de l'utilisateur connecté
    private String nom;
    private String email;
    private String role;   // "CLIENT", "AGENT", "DIRECTEUR"
    private Long   idUtilisateur;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /** Connecte un utilisateur */
    public void login(Long id, String nom, String email, String role) {
        this.idUtilisateur = id;
        this.nom   = nom;
        this.email = email;
        this.role  = role;
    }

    /** Déconnecte l'utilisateur */
    public void logout() {
        this.idUtilisateur = null;
        this.nom   = null;
        this.email = null;
        this.role  = null;
    }

    public boolean isLoggedIn()     { return role != null; }
    public boolean isClient()       { return "CLIENT".equals(role); }
    public boolean isAgent()        { return "AGENT".equals(role); }
    public boolean isDirecteur()    { return "DIRECTEUR".equals(role); }

    public Long   getIdUtilisateur() { return idUtilisateur; }
    public String getNom()           { return nom; }
    public String getEmail()         { return email; }
    public String getRole()          { return role; }
}