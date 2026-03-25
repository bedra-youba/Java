package com.microfinance.views;

import com.microfinance.Util.Validation;
import com.microfinance.controlleur.DirecteurController;
import com.microfinance.model.Agent;
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
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GestionEmployesViewController implements Initializable {

    @FXML private TextField txtNom, txtPrenom, txtEmail, txtTel, txtMatricule, txtPoste;
    @FXML private TableView<Agent>            tableAgents;
    @FXML private TableColumn<Agent, Long>    colId;
    @FXML private TableColumn<Agent, String>  colNom, colPrenom, colEmail, colMatricule, colPoste;
    @FXML private Label lblMessage;

    private final DirecteurController controller = new DirecteurController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colPoste.setCellValueFactory(new PropertyValueFactory<>("poste"));
        charger();
    }

    @FXML
    public void onAjouterAgent() {
        if (txtNom.getText().isBlank() || txtMatricule.getText().isBlank()) {
            afficherErreur("Nom et matricule obligatoires."); return;
        }

        // ← Validation téléphone
        if (!txtTel.getText().isBlank()
                && !Validation.telephoneValide(txtTel.getText())) {
            afficherErreur(Validation.messageTelephone()); return;
        }

        Agent a = new Agent();
        a.setNom(txtNom.getText());
        a.setPrenom(txtPrenom.getText());
        a.setEmail(txtEmail.getText());
        a.setTelephone(txtTel.getText());
        a.setMatricule(txtMatricule.getText());
        a.setPoste(txtPoste.getText());
        a.setRole("AGENT");
        a.setDateEmbauche(LocalDate.now());
        a.setLogin(txtMatricule.getText());
        a.setMotDePasse("changeme");

        if (controller.ajouterAgent(a)) {
            afficherSucces("Employé ajouté !");
            charger(); vider();
        } else afficherErreur("Matricule déjà utilisé ou erreur.");
    }
    @FXML
    public void onSupprimerAgent() {
        Agent s = tableAgents.getSelectionModel().getSelectedItem();
        if (s == null) { afficherErreur("Sélectionnez un employé."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + s.getNom() + " ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                if (controller.supprimerAgent(s.getIdUtilisateur())) {
                    afficherSucces("Supprimé !"); charger();
                } else afficherErreur("Erreur suppression.");
            }
        });
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardDirecteurView.fxml"));
            Stage stage = (Stage) tableAgents.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void charger() {
        tableAgents.setItems(
                FXCollections.observableArrayList(controller.getTousLesAgents()));
    }
    private void vider() {
        txtNom.clear(); txtPrenom.clear(); txtEmail.clear();
        txtTel.clear(); txtMatricule.clear(); txtPoste.clear();
    }
    private void afficherSucces(String m) {
        lblMessage.setStyle("-fx-text-fill: green;"); lblMessage.setText(m);
    }
    private void afficherErreur(String m) {
        lblMessage.setStyle("-fx-text-fill: red;"); lblMessage.setText(m);
    }
}
