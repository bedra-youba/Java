

package com.microfinance.model;

public class Directeur extends Utilisateur {

    private String telephone;

    // Méthodes métier
    public void consulterStatistiques() {}
    public void gererAgents() {}
    public void validerContrats() {}
    public void consulterRapports() {}
    public void configurerSysteme() {}

    // Getter et Setter
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}