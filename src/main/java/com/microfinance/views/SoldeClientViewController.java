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

public class SoldeClientViewController implements Initializable {

    @FXML private Label lblSoldeAffiche;
    @FXML private Label lblMessageSolde;
    @FXML private TableView<CompteEpargne>           tableComptes;
    @FXML private TableColumn<CompteEpargne, Long>   colIdCompte;
    @FXML private TableColumn<CompteEpargne, Double> colSolde;
    @FXML private TableColumn<CompteEpargne, String> colDateCreation;
    @FXML private TableColumn<CompteEpargne, String> colStatut;

    private final ClientController controller = new ClientController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdCompte.setCellValueFactory(new PropertyValueFactory<>("idCompte"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCreation.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateCreation() != null
                                ? cellData.getValue().getDateCreation().toLocalDate().toString()
                                : "—"
                )
        );

        // Charger uniquement les comptes du client connecté
        Long id = SessionClient.getIdClient();
        tableComptes.setItems(FXCollections.observableArrayList(
                controller.voirMesComptes(id)));

        // Calculer le solde total
        double total = controller.voirMesComptes(id).stream()
                .mapToDouble(CompteEpargne::getSolde).sum();
        lblSoldeAffiche.setText(String.format("%.2f MRU", total));
        lblSoldeAffiche.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardClient.fxml"));
            Stage stage = (Stage) tableComptes.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}