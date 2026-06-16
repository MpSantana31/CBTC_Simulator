package org.example.cbtc_simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SeedData.seedCompleto();
        SeedData.resetarViagens();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "view/controller/tela_principal.fxml"));
        stage.setTitle("CBTC Simulator");
        stage.setScene(new Scene(loader.load(), 1000, 700));
        stage.show();
    }
}
