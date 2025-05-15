package com.mpm.speakupdesk.commonutils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;


public class CustomAlerts {
    // Variables estáticas para la alerta en la UI
    private static HBox alertContainer;
    private static Label alertLabel;

    // 🔹 Alerta de Confirmación con Mensaje Personalizado
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Agregar botones personalizados
        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSi, btnNo);

        // Bloquear interacción fuera del modal
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initModality(Modality.APPLICATION_MODAL); // Hace que sea modal

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnSi;
    }

    public static void setAlertComponents(HBox container, Label label) {
        alertContainer = container;
        alertLabel = label;
    }

    private static void showAlert(String message, String... styleClasses) {
        if (alertContainer == null || alertLabel == null) {
            System.err.println("Error: alertContainer o alertLabel no están inicializados.");
            return;
        }

        Platform.runLater(() -> {
            alertLabel.setText(message);

            alertContainer.getStyleClass().setAll("alert-box");
            alertLabel.getStyleClass().setAll();
            alertContainer.getStyleClass().addAll(styleClasses);
            alertLabel.getStyleClass().add("alert-text");

            // Crear el ícono según el tipo de alerta
            FontAwesomeIconView icon = new FontAwesomeIconView();
            icon.setGlyphSize(25);

            // 🔹 Clase adicional según tipo
            for (String style : styleClasses) {
                if (styleClasses[0].contains("success")) {
                    alertLabel.getStyleClass().add("alert-text1");
                    icon.setIcon(FontAwesomeIcon.CHECK_CIRCLE);
                    icon.setFill(Color.rgb(76, 175, 80, 0.8));
                } else if (styleClasses[0].contains("warning")) {
                    alertLabel.getStyleClass().add("alert-text2");
                    icon.setIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
                    icon.setFill(Color.rgb(212, 154, 6));
                } else if (styleClasses[0].contains("danger")) {
                    icon.setIcon(FontAwesomeIcon.EXCLAMATION_CIRCLE);
                    icon.setFill(Color.WHITE);
                }
            }
            // Combinar el ícono con el texto
            alertLabel.setText(message);
            alertLabel.setGraphic(icon);

            alertContainer.setVisible(true);
            // Ocultar después de 3 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> alertContainer.setVisible(false));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    // 🔹 Alerta de Éxito y Error con Mensaje Personalizado
    public static void mostrarExito(String mensaje) {showAlert(mensaje, "alert-success", "alert-text");}
    public static void mostrarError(String mensaje) {showAlert(mensaje, "alert-danger", "alert-text");}
    public static void mostrarAdvertencia(String mensaje){ showAlert(mensaje, "alert-warning", "alert-text"); }
}

