package com.mpm.speakupdesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML desde la ubicación correcta
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        // Configurar la escena
        Scene scene = new Scene(root, 400, 500);
        // Cargar el archivo CSS
        scene.getStylesheets().add(getClass().getResource("/css/login.css").toExternalForm());
        // Configurar la ventana
        stage.setTitle("SpeakUP! - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}