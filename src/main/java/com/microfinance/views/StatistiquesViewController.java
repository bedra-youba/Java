package com.microfinance.views;


import com.microfinance.controlleur.DirecteurController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class StatistiquesViewController implements Initializable {

    @FXML private Label lblCapital;
    @FXML private Label lblAgences;
    @FXML private Label lblAgents;

    private final DirecteurController controller = new DirecteurController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblCapital.setText(String.format("%.0f MRU", controller.getCapitalTotal()));
        lblAgences.setText(String.valueOf(controller.getNombreAgences()));
        lblAgents.setText(String.valueOf(controller.getNombreAgents()));
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardDirecteurView.fxml"));
            Stage stage = (Stage) lblCapital.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}