
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
        import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class GestionAgencesViewController implements Initializable {

    @FXML private TextField txtNomAgence, txtVilleAgence, txtTelAgence, txtCapitalAgence;
    @FXML private TableView<Agence>           tableAgences;
    @FXML private TableColumn<Agence, Long>   colId;
    @FXML private TableColumn<Agence, String> colNom, colVille, colTel;
    @FXML private TableColumn<Agence, Double> colCapital;
    @FXML private Label lblMessage;

    private final DirecteurController controller = new DirecteurController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idAgence"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomAgence"));
        colVille.setCellValueFactory(new PropertyValueFactory<>("ville"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colCapital.setCellValueFactory(new PropertyValueFactory<>("capital"));
        charger();
    }

    @FXML
    public void onAjouterAgence() {
        try {
            if (txtNomAgence.getText().isBlank()) {
                afficherErreur("Nom agence obligatoire.");
                return;
            }
            Agence a = new Agence();
            a.setNomAgence(txtNomAgence.getText());
            a.setVille(txtVilleAgence.getText());
            a.setTelephone(txtTelAgence.getText());
            a.setCapital(txtCapitalAgence.getText().isBlank() ? 0
                    : Double.parseDouble(txtCapitalAgence.getText()));
            if (controller.ajouterAgence(a)) {
                afficherSucces("Agence ajoutée !");
                charger();
                vider();
            } else afficherErreur("Erreur lors de l'ajout.");
        } catch (NumberFormatException e) {
            afficherErreur("Capital doit être un nombre.");
        }
    }

    @FXML
    public void onModifierAgence() {
        Agence s = tableAgences.getSelectionModel().getSelectedItem();
        if (s == null) { afficherErreur("Sélectionnez une agence."); return; }
        if (!txtNomAgence.getText().isBlank()) s.setNomAgence(txtNomAgence.getText());
        if (!txtVilleAgence.getText().isBlank()) s.setVille(txtVilleAgence.getText());
        if (!txtTelAgence.getText().isBlank()) s.setTelephone(txtTelAgence.getText());
        if (!txtCapitalAgence.getText().isBlank())
            s.setCapital(Double.parseDouble(txtCapitalAgence.getText()));
        if (controller.modifierAgence(s)) {
            afficherSucces("Agence modifiée !");
            charger(); vider();
        } else afficherErreur("Erreur modification.");
    }

    @FXML
    public void onSupprimerAgence() {
        Agence s = tableAgences.getSelectionModel().getSelectedItem();
        if (s == null) { afficherErreur("Sélectionnez une agence."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + s.getNomAgence() + " ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                if (controller.supprimerAgence(s.getIdAgence())) {
                    afficherSucces("Supprimée !"); charger();
                } else afficherErreur("Erreur suppression.");
            }
        });
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardDirecteurView.fxml"));
            Stage stage = (Stage) tableAgences.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void charger() {
        tableAgences.setItems(
                FXCollections.observableArrayList(controller.getToutesLesAgences()));
    }
    private void vider() {
        txtNomAgence.clear(); txtVilleAgence.clear();
        txtTelAgence.clear(); txtCapitalAgence.clear();
    }
    private void afficherSucces(String m) {
        lblMessage.setStyle("-fx-text-fill: green;"); lblMessage.setText(m);
    }
    private void afficherErreur(String m) {
        lblMessage.setStyle("-fx-text-fill: red;"); lblMessage.setText(m);
    }
}