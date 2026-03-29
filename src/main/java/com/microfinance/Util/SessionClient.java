
// util/SessionClient.java
package com.microfinance.Util;

public class SessionClient {
    private static Long idClient = null;
    private static String nomClient = "";

    public static void connecter(Long id, String nom) {
        idClient = id;
        nomClient = nom;
    }

    public static Long getIdClient() { return idClient; }
    public static String getNomClient() { return nomClient; }

    public static boolean estConnecte() { return idClient != null; }

    public static void deconnecter() {
        idClient = null;
        nomClient = "";
    }
}