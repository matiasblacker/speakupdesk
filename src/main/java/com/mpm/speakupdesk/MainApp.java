package com.mpm.speakupdesk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        //detectar las clases donde hay mal cierre de conexion
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        // Cargar el archivo FXML desde la ubicación correcta
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();
        // Configurar la escena
        Scene scene = new Scene(root, 400, 640);
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