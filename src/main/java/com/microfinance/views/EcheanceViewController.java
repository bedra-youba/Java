package com.microfinance.views;



import com.microfinance.controlleur.ContratController;
import com.microfinance.model.ContratMourabaha;
import com.microfinance.model.Echeance;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
        import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class EcheanceViewController implements Initializable {

    @FXML private Label                             lblContrat;
    @FXML private TableView<Echeance>               tableEcheances;
    @FXML private TableColumn<Echeance, Integer>    colNumero;
    @FXML private TableColumn<Echeance, String>     colDate;
    @FXML private TableColumn<Echeance, Double>     colMontant;
    @FXML private TableColumn<Echeance, String>     colStatut;
    @FXML private Label                             lblMessage;

    private final ContratController controller = new ContratController();
    private ContratMourabaha contratCourant;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroEchange"));
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateEchange() != null
                                ? cellData.getValue().getDateEchange().toLocalDate().toString()
                                : "—"
                )
        );
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));
    }

    // Appelé depuis ContratViewController pour passer le contrat sélectionné
    public void setContrat(ContratMourabaha contrat) {
        this.contratCourant = contrat;
        lblContrat.setText("Contrat #" + contrat.getIdContrat()
                + " — Client : " + (contrat.getClient() != null ? contrat.getClient().getNom() : "—"));
        chargerEcheances();
    }

    @FXML
    public void onPayerEcheance() {
        Echeance selected = tableEcheances.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherErreur("Sélectionnez une échéance.");
            return;
        }
        if ("PAYE".equals(selected.getStatutPaiement())) {
            afficherErreur("Cette échéance est déjà payée.");
            return;
        }
        boolean ok = controller.payerEcheance(selected.getIdEchange());
        if (ok) {
            afficherSucces("Échéance #" + selected.getNumeroEchange() + " marquée comme payée.");
            chargerEcheances();
        } else {
            afficherErreur("Erreur lors du paiement.");
        }
    }

    private void chargerEcheances() {
        tableEcheances.setItems(
                FXCollections.observableArrayList(
                        controller.getEcheancesContrat(contratCourant.getIdContrat())
                )
        );
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