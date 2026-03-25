package com.microfinance.views;

import com.microfinance.controlleur.AgentController;
import com.microfinance.model.DemandeAchat;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DemandesAchatViewController implements Initializable {

    // Table gauche — demandes clients EN_ATTENTE
    @FXML private TableView<DemandeAchat>           tableDemandesClients;
    @FXML private TableColumn<DemandeAchat, Long>   colIdClient;
    @FXML private TableColumn<DemandeAchat, String> colDateClient;
    @FXML private TableColumn<DemandeAchat, String> colDescClient;
    @FXML private TableColumn<DemandeAchat, Double> colPrixClient;
    @FXML private TableColumn<DemandeAchat, String> colStatClient;
    @FXML private Label lblMessageGauche;

    // Table droite — demandes enregistrées par l'agent
    @FXML private TableView<DemandeAchat>           tableDemandesAgent;
    @FXML private TableColumn<DemandeAchat, Long>   colIdAgent;
    @FXML private TableColumn<DemandeAchat, String> colDateAgent;
    @FXML private TableColumn<DemandeAchat, String> colDescAgent;
    @FXML private TableColumn<DemandeAchat, Double> colPrixAgent;
    @FXML private TableColumn<DemandeAchat, String> colStatAgent;
    @FXML private Label lblMessageDroit;

    private final AgentController controller = new AgentController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes(colIdClient, colDateClient, colDescClient,
                colPrixClient, colStatClient);
        configurerColonnes(colIdAgent, colDateAgent, colDescAgent,
                colPrixAgent, colStatAgent);
        charger();
    }

    private void configurerColonnes(
            TableColumn<DemandeAchat, Long>   colId,
            TableColumn<DemandeAchat, String> colDate,
            TableColumn<DemandeAchat, String> colDesc,
            TableColumn<DemandeAchat, Double> colPrix,
            TableColumn<DemandeAchat, String> colStat) {

        colId.setCellValueFactory(new PropertyValueFactory<>("idDemande"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descriptionBien"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixEstime"));
        colStat.setCellValueFactory(new PropertyValueFactory<>("statutDemande"));
        colDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getDateDemande() != null
                                ? cellData.getValue().getDateDemande().toLocalDate().toString()
                                : "—"
                )
        );
    }

    // Agent clique sur une demande EN_ATTENTE et l'enregistre
    @FXML
    public void onEnregistrerDemandeSelectionnee() {
        DemandeAchat selected = tableDemandesClients
                .getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessageGauche.setStyle("-fx-text-fill: red;");
            lblMessageGauche.setText("Sélectionnez une demande client.");
            return;
        }
        if (!"EN_ATTENTE".equals(selected.getStatutDemande())) {
            lblMessageGauche.setStyle("-fx-text-fill: orange;");
            lblMessageGauche.setText("Cette demande a déjà été traitée.");
            return;
        }

        // Changer le statut en ENREGISTREE
        if (controller.changerStatutDemande(selected.getIdDemande(), "ENREGISTREE")) {
            lblMessageGauche.setStyle("-fx-text-fill: green;");
            lblMessageGauche.setText("✅ Demande #" + selected.getIdDemande()
                    + " enregistrée !");
            lblMessageDroit.setStyle("-fx-text-fill: green;");
            lblMessageDroit.setText("Demande ajoutée à votre liste.");
            charger();
        } else {
            lblMessageGauche.setStyle("-fx-text-fill: red;");
            lblMessageGauche.setText("Erreur lors de l'enregistrement.");
        }
    }

    private void charger() {
        List<DemandeAchat> toutes = controller.getToutesLesDemandes();

        // Gauche : uniquement EN_ATTENTE (soumises par clients)
        List<DemandeAchat> enAttente = toutes.stream()
                .filter(d -> "EN_ATTENTE".equals(d.getStatutDemande()))
                .collect(Collectors.toList());
        tableDemandesClients.setItems(
                FXCollections.observableArrayList(enAttente));

        // Droite : uniquement ENREGISTREE (traitées par agent)
        List<DemandeAchat> enregistrees = toutes.stream()
                .filter(d -> "ENREGISTREE".equals(d.getStatutDemande()))
                .collect(Collectors.toList());
        tableDemandesAgent.setItems(
                FXCollections.observableArrayList(enregistrees));
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardAgent.fxml"));
            Stage stage = (Stage) tableDemandesClients.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}

