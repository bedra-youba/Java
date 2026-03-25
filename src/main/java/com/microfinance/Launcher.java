
package com.microfinance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                //getClass().getResource("/fxml/DashboardAgent.fxml")
                //getClass().getResource("/fxml/DashboardClient.fxml")
                getClass().getResource("/fxml/DashboardDirecteurView.fxml")
        );
        Parent root = loader.load();

        primaryStage.setTitle("Microfinance — Contrats Mourabaha");
        primaryStage.setScene(new Scene(root, 750, 650));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


//pour la client

//package com.microfinance;
//
//import com.microfinance.Util.DatabaseConnection;
//import com.microfinance.Util.SessionClient;
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//public class Launcher extends Application {
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//
//        // ← Simulation session client pour test (mettre un ID qui existe dans ta base)
//        SessionClient.connecter(1L, "Client Test");
//
//        FXMLLoader loader = new FXMLLoader(
//                getClass().getResource("/fxml/DashboardClient.fxml")
//        );
//        Parent root = loader.load();
//        primaryStage.setTitle("MicroFinance MRU — Client");
//        primaryStage.setScene(new Scene(root, 1100, 750));
//        primaryStage.setMaximized(true);
//        primaryStage.setOnCloseRequest(e -> DatabaseConnection.closePool());
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}