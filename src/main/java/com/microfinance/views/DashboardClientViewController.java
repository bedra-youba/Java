package com.microfinance.views;

import com.microfinance.controlleur.ClientController;
import com.microfinance.model.CompteEpargne;
import com.microfinance.Util.SessionClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardClientViewController implements Initializable {

    @FXML private Label lblBienvenue;
    @FXML private Label lblNomClient;
    @FXML private Label lblSolde;
    @FXML private Label lblNombreComptes;
    @FXML private Label lblNombreDemandes;

    private final ClientController controller = new ClientController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Afficher le nom du client connecté
        lblNomClient.setText(SessionClient.getNomClient());
        lblBienvenue.setText("Bienvenue, " + SessionClient.getNomClient() + " 👋");

        // Charger ses données uniquement
        chargerDonnees();
    }

    private void chargerDonnees() {
        Long id = SessionClient.getIdClient();

        // Ses comptes
        List<CompteEpargne> comptes = controller.voirMesComptes(id);
        lblNombreComptes.setText(String.valueOf(comptes.size()));

        // Son solde total
        double soldeTotal = comptes.stream()
                .mapToDouble(CompteEpargne::getSolde).sum();
        lblSolde.setText(String.format("%.2f MRU", soldeTotal));

        // Ses demandes uniquement
        lblNombreDemandes.setText(String.valueOf(
                controller.voirMesDemandes(id).size()));
    }

    @FXML public void ouvrirSolde()      { ouvrirPage("/fxml/SoldeClient.fxml"); }
    @FXML public void ouvrirHistorique() { ouvrirPage("/fxml/HistoriqueClient.fxml"); }
    @FXML public void ouvrirDemande()    { ouvrirPage("/fxml/DemandeAchatClient.fxml"); }

    @FXML
    public void onDeconnexion() {
        SessionClient.deconnecter();
        ouvrirPage("/fxml/Login.fxml");
    }

    private void ouvrirPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) lblNomClient.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}