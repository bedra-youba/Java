package com.microfinance.views;

import com.microfinance.controlleur.DirecteurController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardDirecteurViewController implements Initializable {

    @FXML private Label lblCapitalTotal;
    @FXML private Label lblNombreAgences;
    @FXML private Label lblNombreAgents;

    private final DirecteurController controller = new DirecteurController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblCapitalTotal.setText(String.format("%.0f MRU", controller.getCapitalTotal()));
        lblNombreAgences.setText(String.valueOf(controller.getNombreAgences()));
        lblNombreAgents.setText(String.valueOf(controller.getNombreAgents()));
    }

    @FXML
    public void ouvrirGestionAgences() { ouvrirPage("/fxml/GestionAgences.fxml"); }

    @FXML
    public void ouvrirGestionEmployes() { ouvrirPage("/fxml/GestionEmployes.fxml"); }

    @FXML
    public void ouvrirStatistiques() { ouvrirPage("/fxml/Statistiques.fxml"); }

    @FXML
    public void ouvrirBenefices() { ouvrirPage("/fxml/Benefices.fxml"); }

    private void ouvrirPage(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) lblCapitalTotal.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}