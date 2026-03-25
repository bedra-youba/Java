package com.microfinance.views;


import com.microfinance.controlleur.DirecteurController;
import com.microfinance.model.Agence;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class BeneficesViewController implements Initializable {

    @FXML private ComboBox<Agence> comboAgences;
    @FXML private Label lblBenefice;

    private final DirecteurController controller = new DirecteurController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboAgences.setItems(
                FXCollections.observableArrayList(controller.getToutesLesAgences()));
        comboAgences.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Agence a) {
                return a != null ? a.getNomAgence() + " — " + a.getVille() : "";
            }
            @Override public Agence fromString(String s) { return null; }
        });
    }

    @FXML
    public void onVoirBenefice() {
        Agence s = comboAgences.getSelectionModel().getSelectedItem();
        if (s == null) {
            lblBenefice.setStyle("-fx-text-fill: red;");
            lblBenefice.setText("Sélectionnez une agence.");
            return;
        }
        double b = controller.getBeneficeParAgence(s.getIdAgence());
        lblBenefice.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
        lblBenefice.setText("Bénéfice de " + s.getNomAgence()
                + " : " + String.format("%.2f MRU", b));
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardDirecteurView.fxml"));
            Stage stage = (Stage) comboAgences.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}