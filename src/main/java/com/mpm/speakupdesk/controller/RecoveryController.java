package com.mpm.speakupdesk.controller;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.service.PasswordResetTokenService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RecoveryController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;
    @FXML private Button sendCodeButton;
    @FXML private Button validateButton;
    @FXML private VBox codeVerificationPane;
    @FXML private VBox emailPane;

    private Stage stage;
    private String currentEmail;

    @FXML
    public void initialize() {
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
        emailPane.setVisible(true);
        emailPane.setManaged(true);
        codeVerificationPane.setVisible(false);
        codeVerificationPane.setManaged(false); // Ocultar campo de código inicialmente
    }

    // Enviar código al correo
    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();

        // Validar email
        if (email.isEmpty()) {
            CustomAlerts.mostrarAdvertencia("Ingresa tu correo electrónico");
            return;
        }
        if (!isValidEmail(email)) {
            CustomAlerts.mostrarAdvertencia("Correo electrónico inválido");
            return;
        }
            // Deshabilitar botón mientras se procesa
        sendCodeButton.setDisable(true);
        currentEmail = email;

        PasswordResetTokenService.sendPasswordResetCode(email)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        CustomAlerts.mostrarExito("Código enviado");
                        showCodeVerificationPane();
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        handleSendCodeError(ex);
                    });
                    return null;
                });
    }

    // Validar código y cambiar contraseña
    @FXML
    private void handleValidateCode() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            CustomAlerts.mostrarAdvertencia("Ingresa el código de verificación");
            return;
        }

        if (currentEmail == null || currentEmail.isEmpty()) {
            CustomAlerts.mostrarError("Error: Email no encontrado.");
            return;
        }

        // Deshabilitar botón mientras se procesa
        validateButton.setDisable(true);
        validateButton.setText("Validando...");

        PasswordResetTokenService.validateCodeAndResetPassword(currentEmail, code)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response != null && !response.trim().isEmpty()) {
                            handleValidationSuccess();
                        } else {
                            CustomAlerts.mostrarError("Error del servidor");
                            validateButton.setDisable(false);
                            validateButton.setText("Validar Código");
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        handleValidationError(ex);
                        validateButton.setDisable(false);
                        validateButton.setText("Validar Código");
                    });
                    return null;
                });
    }

    private void showCodeVerificationPane() {
        // Ocultar todo el panel de email
        emailPane.setVisible(false);
        emailPane.setManaged(false);
        // Ocultar específicamente el botón de enviar código
        sendCodeButton.setVisible(false);
        sendCodeButton.setManaged(false);
        // Mostrar el panel de verificación de código
        codeVerificationPane.setVisible(true);
        codeVerificationPane.setManaged(true);
        // Enfocar el campo de código
        codeField.requestFocus();
    }

    private void handleSendCodeError(Throwable ex) {
        String errorMessage = "Error al enviar el código";

        if (ex.getCause() instanceof java.net.ConnectException) {
            errorMessage = "Verifica tu conexión a internet";
        } else if (ex.getMessage() != null) {
            if (ex.getMessage().contains("404")) {
                errorMessage = "Correo no encontrado";
            } else if (ex.getMessage().contains("500")) {
                errorMessage = "Error interno del servidor";
            }
        }
        CustomAlerts.mostrarError(errorMessage);
    }

    private void handleValidationError(Throwable ex) {
        String errorMessage = "Código inválido o expirado";

        if (ex.getCause() instanceof java.net.ConnectException) {
            errorMessage = "No se puede conectar al servidor";
        } else if (ex.getMessage() != null) {
            if (ex.getMessage().contains("400")) {
                errorMessage = "Código inválido o expirado";
            } else if (ex.getMessage().contains("404")) {
                errorMessage = "Solicitud de recuperación no encontrada";
            } else if (ex.getMessage().contains("500")) {
                errorMessage = "Error interno del servidor.";
            }
        }
        CustomAlerts.mostrarError(errorMessage);
    }

    private void handleValidationSuccess() {
        CustomAlerts.mostrarExito("¡Contraseña restablecida!\nRevisa tu correo");
        // Esperar un momento antes de cerrar para que el usuario lea el mensaje
        CompletableFuture.delayedExecutor(2, java.util.concurrent.TimeUnit.SECONDS)
                .execute(() -> Platform.runLater(() -> {
                    returnToLogin();
                }));
    }

    private void returnToLogin() {
        try {
            // Cargar la ventana de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Iniciar Sesión - SpeakUP!");
            loginStage.setResizable(false);

            // Cerrar la ventana actual
            if (stage != null) {
                stage.close();
            }

            loginStage.show();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el login");
            e.printStackTrace();

            // Si no se puede cargar login, al menos cerrar esta ventana
            if (stage != null) {
                stage.close();
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleCancel() {
        returnToLogin();
    }

}
