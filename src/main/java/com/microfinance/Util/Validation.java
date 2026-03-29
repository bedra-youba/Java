package com.microfinance.Util;


public class Validation {

    /**
     * NNI : exactement 10 chiffres
     */
    public static boolean nniValide(String nni) {
        if (nni == null) return false;
        return nni.matches("\\d{10}");
    }

    /**
     * Téléphone : exactement 8 chiffres, commence par 2, 3 ou 4
     */
    public static boolean telephoneValide(String tel) {
        if (tel == null) return false;
        return tel.matches("[234]\\d{7}");
    }

    /**
     * Messages d'erreur clairs
     */
    public static String messageNNI() {
        return "NNI invalide — doit contenir exactement 10 chiffres.";
    }

    public static String messageTelephone() {
        return "Téléphone invalide — 8 chiffres, commençant par 2, 3 ou 4.";
    }
}
