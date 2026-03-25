package com.microfinance.views;


import com.microfinance.controlleur.AgentController;
import com.microfinance.model.Client;
import com.microfinance.model.CompteEpargne;
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

public class CompteEpargneViewController implements Initializable {

    @FXML private TextField txtClientIdCompte, txtSoldeInitial;
    @FXML private TextField txtIdCompteOperation, txtMontantOperation;
    @FXML private TextField txtRechercheClient;
    @FXML private TableView<CompteEpargne>               tableComptes;
    @FXML private TableColumn<CompteEpargne, Long>       colIdCompte;
    @FXML private TableColumn<CompteEpargne, Double>     colSolde;
    @FXML private TableColumn<CompteEpargne, String>     colDateCreation, colStatutCompte;
    @FXML private Label lblMessageCompte, lblMessageOperation;

    private final AgentController controller = new AgentController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colIdCompte.setCellValueFactory(new PropertyValueFactory<>("idCompte"));
        colSolde.setCellValueFactory(new PropertyValueFactory<>("solde"));
        colStatutCompte.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCreation.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateCreation() != null
                                ? cellData.getValue().getDateCreation().toLocalDate().toString()
                                : "—"
                )
        );
    }

    @FXML
    public void onCreerCompte() {
        try {
            if (txtClientIdCompte.getText().isBlank()) {
                afficherErreurCompte("ID client obligatoire."); return;
            }
            Client c = new Client();
            c.setIdClient(Long.parseLong(txtClientIdCompte.getText()));

            CompteEpargne cp = new CompteEpargne();
            cp.setClient(c);
            cp.setSolde(txtSoldeInitial.getText().isBlank() ? 0
                    : Double.parseDouble(txtSoldeInitial.getText()));
            cp.setDateCreation(LocalDateTime.now());
            cp.setStatut("ACTIF");

            if (controller.creerCompteEpargne(cp)) {
                afficherSuccesCompte("Compte créé !");
                txtClientIdCompte.clear(); txtSoldeInitial.clear();
            } else afficherErreurCompte("Erreur création compte.");
        } catch (NumberFormatException e) {
            afficherErreurCompte("Valeurs invalides.");
        }
    }

    @FXML
    public void onDeposer() {
        try {
            Long id = Long.parseLong(txtIdCompteOperation.getText());
            double montant = Double.parseDouble(txtMontantOperation.getText());
            if (controller.deposer(id, montant)) {
                afficherSuccesOp("Dépôt enregistré : +" + montant + " MRU");
                onRechercherComptes();
            } else afficherErreurOp("Erreur dépôt.");
        } catch (NumberFormatException e) {
            afficherErreurOp("ID et montant doivent être des nombres.");
        }
    }

    @FXML
    public void onRetirer() {
        try {
            Long id = Long.parseLong(txtIdCompteOperation.getText());
            double montant = Double.parseDouble(txtMontantOperation.getText());
            if (controller.retirer(id, montant)) {
                afficherSuccesOp("Retrait enregistré : -" + montant + " MRU");
                onRechercherComptes();
            } else afficherErreurOp("Solde insuffisant ou erreur.");
        } catch (NumberFormatException e) {
            afficherErreurOp("ID et montant doivent être des nombres.");
        }
    }

    @FXML
    public void onRechercherComptes() {
        try {
            Long clientId = Long.parseLong(txtRechercheClient.getText());
            tableComptes.setItems(FXCollections.observableArrayList(
                    controller.getComptesClient(clientId)));
        } catch (NumberFormatException e) {
            afficherErreurCompte("ID client invalide.");
        }
    }

    @FXML
    public void retourDashboard() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/DashboardAgent.fxml"));
            Stage stage = (Stage) tableComptes.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void afficherSuccesCompte(String m) {
        lblMessageCompte.setStyle("-fx-text-fill: green;"); lblMessageCompte.setText(m);
    }
    private void afficherErreurCompte(String m) {
        lblMessageCompte.setStyle("-fx-text-fill: red;"); lblMessageCompte.setText(m);
    }
    private void afficherSuccesOp(String m) {
        lblMessageOperation.setStyle("-fx-text-fill: green;"); lblMessageOperation.setText(m);
    }
    private void afficherErreurOp(String m) {
        lblMessageOperation.setStyle("-fx-text-fill: red;"); lblMessageOperation.setText(m);
    }
}