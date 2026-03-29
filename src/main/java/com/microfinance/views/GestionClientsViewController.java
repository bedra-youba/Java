package com.microfinance.views;


import com.microfinance.Util.Validation;
import com.microfinance.controlleur.AgentController;
import com.microfinance.model.Client;
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

public class GestionClientsViewController implements Initializable {

    @FXML private TextField txtNom, txtNNI, txtTelephone;
    @FXML private TextField txtAdresse, txtProfession, txtRevenu;
    @FXML private TableView<Client>            tableClients;
    @FXML private TableColumn<Client, Long>    colId;
    @FXML private TableColumn<Client, String>  colNom, colNNI, colTelephone;
    @FXML private TableColumn<Client, String>  colAdresse, colProfession, colStatut;
    @FXML private TableColumn<Client, Double>  colRevenu;
    @FXML private Label lblMessage;

    private final AgentController controller = new AgentController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colNNI.setCellValueFactory(new PropertyValueFactory<>("NNI"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colProfession.setCellValueFactory(new PropertyValueFactory<>("profession"));
        colRevenu.setCellValueFactory(new PropertyValueFactory<>("revenuMensuel"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutClient"));
        charger();
    }

    @FXML
    public void onAjouterClient() {
        try {
            if (txtNom.getText().isBlank() || txtNNI.getText().isBlank()) {
                afficherErreur("Nom et NNI obligatoires."); return;
            }

            // ← Validation NNI
            if (!Validation.nniValide(txtNNI.getText())) {
                afficherErreur(Validation.messageNNI()); return;
            }

            // ← Validation Téléphone
            if (!txtTelephone.getText().isBlank()
                    && !Validation.telephoneValide(txtTelephone.getText())) {
                afficherErreur(Validation.messageTelephone()); return;
            }

            if (controller.nniExiste(txtNNI.getText())) {
                afficherErreur("NNI déjà existant !"); return;
            }

            Client c = new Client();
            c.setNom(txtNom.getText());
            c.setNNI(txtNNI.getText());
            c.setTelephone(txtTelephone.getText());
            c.setAdresse(txtAdresse.getText());
            c.setProfession(txtProfession.getText());
            c.setRevenuMensuel(txtRevenu.getText().isBlank() ? 0
                    : Double.parseDouble(txtRevenu.getText()));
            c.setDateInscription(LocalDateTime.now());
            c.setStatutClient("ACTIF");

            if (controller.ajouterClient(c)) {
                afficherSucces("Client ajouté !"); charger(); vider();
            } else afficherErreur("Erreur lors de l'ajout.");

        } catch (NumberFormatException e) {
            afficherErreur("Revenu doit être un nombre.");
        }
    }

    @FXML
    public void onModifierClient() {
        Client s = tableClients.getSelectionModel().getSelectedItem();
        if (s == null) { afficherErreur("Sélectionnez un client."); return; }
        if (!txtNom.getText().isBlank()) s.setNom(txtNom.getText());
        if (!txtTelephone.getText().isBlank()) s.setTelephone(txtTelephone.getText());
        if (!txtAdresse.getText().isBlank()) s.setAdresse(txtAdresse.getText());
        if (!txtProfession.getText().isBlank()) s.setProfession(txtProfession.getText());
        if (!txtRevenu.getText().isBlank())
            s.setRevenuMensuel(Double.parseDouble(txtRevenu.getText()));
        if (controller.modifierClient(s)) {
            afficherSucces("Client modifié !"); charger(); vider();
        } else afficherErreur("Erreur modification.");
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardAgent.fxml"));
            Stage stage = (Stage) tableClients.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void charger() {
        tableClients.setItems(
                FXCollections.observableArrayList(controller.getTousLesClients()));
    }
    private void vider() {
        txtNom.clear(); txtNNI.clear(); txtTelephone.clear();
        txtAdresse.clear(); txtProfession.clear(); txtRevenu.clear();
    }
    private void afficherSucces(String m) {
        lblMessage.setStyle("-fx-text-fill: green;"); lblMessage.setText(m);
    }
    private void afficherErreur(String m) {
        lblMessage.setStyle("-fx-text-fill: red;"); lblMessage.setText(m);
    }
}