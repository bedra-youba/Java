package com.microfinance.views;



import com.microfinance.controlleur.AgentController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardAgentViewController implements Initializable {

    @FXML private Label lblNombreClients;
    @FXML private Label lblNombreComptes;
    @FXML private Label lblNombreDemandes;
    @FXML private Label lblNombreFournisseurs;

    private final AgentController controller = new AgentController();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblNombreClients.setText(String.valueOf(controller.getNombreClients()));
        lblNombreDemandes.setText(String.valueOf(
                controller.getToutesLesDemandes().size()));
        lblNombreFournisseurs.setText(String.valueOf(
                controller.getTousLesFournisseurs().size()));
        lblNombreComptes.setText("—");
    }

    @FXML public void ouvrirGestionClients()  { ouvrirPage("/fxml/GestionClients.fxml"); }
    @FXML public void ouvrirCompteEpargne()   { ouvrirPage("/fxml/CompteEpargne.fxml"); }
    @FXML public void ouvrirDemandesAchat()   { ouvrirPage("/fxml/DemandesAchat.fxml"); }
    @FXML public void ouvrirFournisseurs()    { ouvrirPage("/fxml/Fournisseurs.fxml"); }

    private void ouvrirPage(String path) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) lblNombreClients.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
        } catch (Exception e) { e.printStackTrace(); }
    }
}