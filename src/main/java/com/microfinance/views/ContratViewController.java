package com.microfinance.views;



import com.microfinance.controlleur.ContratController;
import com.microfinance.model.Client;
import com.microfinance.model.ContratMourabaha;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ContratViewController implements Initializable {

    // Formulaire
    @FXML private TextField txtClientId;
    @FXML private TextField txtPrixAchat;
    @FXML private TextField txtMarge;
    @FXML private TextField txtDuree;
    @FXML private Label     lblMessage;

    // Table
    @FXML private TableView<ContratMourabaha>               tableContrats;
    @FXML private TableColumn<ContratMourabaha, Long>       colId;
    @FXML private TableColumn<ContratMourabaha, String>     colClient;
    @FXML private TableColumn<ContratMourabaha, Double>     colPrixVente;
    @FXML private TableColumn<ContratMourabaha, Integer>    colDuree;
    @FXML private TableColumn<ContratMourabaha, String>     colStatut;

    private final ContratController controller = new ContratController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Noms exacts des getters dans ContratMourabaha
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrat"));
        colPrixVente.setCellValueFactory(new PropertyValueFactory<>("prixVenteClient"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutContrat"));

        // Colonne client : affiche le nom du client
        colClient.setCellValueFactory(cellData -> {
            Client c = cellData.getValue().getClient();
            return new javafx.beans.property.SimpleStringProperty(
                    c != null ? c.getNom() : "—"
            );
        });

        chargerContrats();
    }

    @FXML
    public void onCreerContrat() {
        try {
            // Validation champs vides
            if (txtClientId.getText().isBlank() || txtPrixAchat.getText().isBlank()
                    || txtMarge.getText().isBlank() || txtDuree.getText().isBlank()) {
                afficherErreur("Veuillez remplir tous les champs.");
                return;
            }

            // Construction du client à partir de l'ID saisi
            Client client = new Client();
            client.setIdClient(Long.parseLong(txtClientId.getText()));

            // Construction du contrat
            ContratMourabaha contrat = new ContratMourabaha();
            contrat.setClient(client);
            contrat.setPrixAchatAgence(Double.parseDouble(txtPrixAchat.getText()));
            contrat.setMargeBeneficiaire(Double.parseDouble(txtMarge.getText()));
            contrat.setDureeMois(Integer.parseInt(txtDuree.getText()));
            contrat.setDateContrat(LocalDateTime.now());
            contrat.setStatutContrat("Acquisition en cours");

            boolean ok = controller.creerContrat(contrat);

            if (ok) {
                afficherSucces("Contrat créé avec " + contrat.getDureeMois() + " échéances !");
                chargerContrats();
                viderFormulaire();
            } else {
                afficherErreur("Erreur lors de la création en base.");
            }

        } catch (NumberFormatException e) {
            afficherErreur("ID client et durée doivent être des nombres entiers.");
        }
    }

    @FXML
    public void onVoirEcheances() {
        ContratMourabaha selected = tableContrats.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Sélectionnez un contrat dans la liste.");
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/EcheanceView.fxml")
            );
            javafx.scene.Parent root = loader.load();
            EcheanceViewController ec = loader.getController();
            ec.setContrat(selected);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Échéances — Contrat #" + selected.getIdContrat());
            stage.setScene(new javafx.scene.Scene(root, 650, 450));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            afficherErreur("Impossible d'ouvrir la fenêtre des échéances.");
        }
    }

    private void chargerContrats() {
        tableContrats.setItems(
                FXCollections.observableArrayList(controller.getTousLesContrats())
        );
    }

    private void viderFormulaire() {
        txtClientId.clear();
        txtPrixAchat.clear();
        txtMarge.clear();
        txtDuree.clear();
    }

    private void afficherSucces(String msg) {
        lblMessage.setStyle("-fx-text-fill: green;");
        lblMessage.setText(msg);
    }

    private void afficherErreur(String msg) {
        lblMessage.setStyle("-fx-text-fill: red;");
        lblMessage.setText(msg);
    }
}