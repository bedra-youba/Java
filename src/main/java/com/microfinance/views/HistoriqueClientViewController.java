package com.microfinance.views;

import com.microfinance.controlleur.ClientController;
import com.microfinance.model.CompteEpargne;
import com.microfinance.Util.SessionClient;
import javafx.beans.property.SimpleStringProperty;
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

public class HistoriqueClientViewController implements Initializable {

    @FXML private TableView<CompteEpargne>           tableHistorique;
    @FXML private TableColumn<CompteEpargne, Long>   colIdC;
    @FXML private TableColumn<CompteEpargne, Double> colSoldeC;
    @FXML private TableColumn<CompteEpargne, String> colDateC;
    @FXML private TableColumn<CompteEpargne, String> colStatutC;

    private final ClientController controller = new ClientController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdC.setCellValueFactory(new PropertyValueFactory<>("idCompte"));
        colSoldeC.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatutC.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateC.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateCreation() != null
                                ? cellData.getValue().getDateCreation().toLocalDate().toString()
                                : "—"
                )
        );

        // Uniquement les comptes du client connecté
        tableHistorique.setItems(FXCollections.observableArrayList(
                controller.voirMesComptes(SessionClient.getIdClient())));
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardClient.fxml"));
            Stage stage = (Stage) tableHistorique.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}