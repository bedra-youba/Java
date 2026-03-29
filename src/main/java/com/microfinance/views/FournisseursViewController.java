package com.microfinance.views;


import com.microfinance.controlleur.AgentController;
import com.microfinance.model.Fournisseur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class FournisseursViewController implements Initializable {

    @FXML private TableView<Fournisseur>           tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Long>   colIdF;
    @FXML private TableColumn<Fournisseur, String> colNomF, colTelF, colEmailF, colAdresseF;

    private final AgentController controller = new AgentController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdF.setCellValueFactory(new PropertyValueFactory<>("idFournisseur"));
        colNomF.setCellValueFactory(new PropertyValueFactory<>("nomEntreprise"));
        colTelF.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmailF.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresseF.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        charger();
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardAgent.fxml"));
            Stage stage = (Stage) tableFournisseurs.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void charger() {
        tableFournisseurs.setItems(FXCollections.observableArrayList(
                controller.getTousLesFournisseurs()));
    }
}

