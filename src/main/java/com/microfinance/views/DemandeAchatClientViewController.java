package com.microfinance.views;

import com.microfinance.controlleur.ClientController;
import com.microfinance.model.Client;
import com.microfinance.model.DemandeAchat;
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
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class DemandeAchatClientViewController implements Initializable {

    @FXML private TextField txtDescriptionClient;
    @FXML private TextField txtPrixEstimeClient;
    @FXML private Label     lblMessageDemandeClient;

    @FXML private TableView<DemandeAchat>           tableMesDemandes;
    @FXML private TableColumn<DemandeAchat, Long>   colIdDem;
    @FXML private TableColumn<DemandeAchat, String> colDateDem;
    @FXML private TableColumn<DemandeAchat, String> colDescDem;
    @FXML private TableColumn<DemandeAchat, Double> colPrixDem;
    @FXML private TableColumn<DemandeAchat, String> colStatDem;

    private final ClientController controller = new ClientController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdDem.setCellValueFactory(new PropertyValueFactory<>("idDemande"));
        colDescDem.setCellValueFactory(new PropertyValueFactory<>("descriptionBien"));
        colPrixDem.setCellValueFactory(new PropertyValueFactory<>("prixEstime"));
        colStatDem.setCellValueFactory(new PropertyValueFactory<>("statutDemande"));
        colDateDem.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateDemande() != null
                                ? cellData.getValue().getDateDemande().toLocalDate().toString()
                                : "—"
                )
        );

        // Charger uniquement ses demandes
        chargerMesDemandes();
    }

    @FXML
    public void onSoumettreDemandeClient() {
        try {
            if (txtDescriptionClient.getText().isBlank()) {
                afficherErreur("Description obligatoire."); return;
            }

            Client c = new Client();
            c.setIdClient(SessionClient.getIdClient()); // ← ID depuis la session

            DemandeAchat d = new DemandeAchat();
            d.setClient(c);
            d.setDescriptionBien(txtDescriptionClient.getText());
            d.setPrixEstime(txtPrixEstimeClient.getText().isBlank() ? 0
                    : Double.parseDouble(txtPrixEstimeClient.getText()));
            d.setDateDemande(LocalDateTime.now());
            d.setStatutDemande("EN_ATTENTE");

            if (controller.faireDemande(d)) {
                afficherSucces("Demande soumise avec succès !");
                txtDescriptionClient.clear();
                txtPrixEstimeClient.clear();
                chargerMesDemandes();
            } else afficherErreur("Erreur lors de la soumission.");
        } catch (NumberFormatException e) {
            afficherErreur("Prix doit être un nombre.");
        }
    }

    private void chargerMesDemandes() {
        // Uniquement les demandes du client connecté
        tableMesDemandes.setItems(FXCollections.observableArrayList(
                controller.voirMesDemandes(SessionClient.getIdClient())));
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardClient.fxml"));
            Stage stage = (Stage) tableMesDemandes.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void afficherSucces(String m) {
        lblMessageDemandeClient.setStyle("-fx-text-fill: green;");
        lblMessageDemandeClient.setText(m);
    }
    private void afficherErreur(String m) {
        lblMessageDemandeClient.setStyle("-fx-text-fill: red;");
        lblMessageDemandeClient.setText(m);
    }
}