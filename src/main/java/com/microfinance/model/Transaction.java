
package com.microfinance.model;

import java.time.LocalDateTime;

public class Transaction {

    private Long idTransaction;
    private String typeTransaction;
    private double montant;
    private LocalDateTime dateTransaction;

    private Client client;

    // Getters et Setters
    public Long getIdTransaction() { return idTransaction; }
    public void setIdTransaction(Long idTransaction) { this.idTransaction = idTransaction; }

    public String getTypeTransaction() { return typeTransaction; }
    public void setTypeTransaction(String typeTransaction) { this.typeTransaction = typeTransaction; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDateTime getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }

    public Client getClient() { return client; }
    }