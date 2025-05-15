package com.mpm.speakupdesk.controller;

import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.service.AuthService;
import com.mpm.speakupdesk.commonutils.CustomAlerts;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;
    @FXML private ImageView imgEye;
    @FXML private Button loginButton; // Asegúrate de tener esta referencia en tu FXML

    private boolean passwordVisible = false;
    private Image imgEyeOpen = new Image(getClass().getResourceAsStream("/img/eye.png"));
    private Image imgEyeClosed = new Image(getClass().getResourceAsStream("/img/eyeslash.png"));

    @FXML
    public void initialize() {
        imgEye.setImage(imgEyeOpen);
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
        visiblePasswordField.setManaged(false); // Ocultar inicialmente el campo visible
    }

    @FXML
    private void handleLogin() {
        // Validar campos vacíos
        if (emailField.getText().isEmpty() ||
                (passwordField.getText().isEmpty() && visiblePasswordField.getText().isEmpty())) {
            CustomAlerts.mostrarError("Email y contraseña son requeridos");
            return;
        }

        loginButton.setDisable(true); // Bloquear botón durante el login

        new Thread(() -> {
            // Obtener contraseña según visibilidad
            String password = passwordVisible ?
                    visiblePasswordField.getText() :
                    passwordField.getText();

            boolean success = AuthService.login(emailField.getText(), password);

            Platform.runLater(() -> {
                loginButton.setDisable(false); // Rehabilitar botón

                if (success) {
                    handleLoginSuccess();
                } else {
                    CustomAlerts.mostrarError("Credenciales incorrectas");
                }
            });
        }).start();
    }

    private void handleLoginSuccess() {
        try {
            LoginResponse usuario = AuthService.getUsuarioLogueado();
            if (usuario == null) {
                CustomAlerts.mostrarError("Error al obtener datos del usuario");
                return;
            }

            Rol rol = usuario.getRol();
            String fxmlPath = switch (rol) {
                case ADMIN_GLOBAL, ADMIN_COLEGIO -> "/views/dashboard/admin.fxml";
                default -> {
                    CustomAlerts.mostrarError("Rol no válido: " + rol);
                    yield null;
                }
            };

            if (fxmlPath != null) {
                loadDashboard(fxmlPath, rol, usuario);
            }
        } catch (Exception e) {
            CustomAlerts.mostrarError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDashboard(String fxmlPath, Rol rol, LoginResponse usuario) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.setMaximized(true);
        newStage.setTitle("SpeakUP! - " + rol);
        newStage.show();

        // Inyectar datos en el controlador
        if (loader.getController() instanceof AdminController) {
            ((AdminController) loader.getController()).initData(usuario);
        }

        // Cerrar ventana de login
        Stage loginStage = (Stage) emailField.getScene().getWindow();
        loginStage.close();
    }

    @FXML
    public void togglePasswordVisibility(ActionEvent actionEvent) {
        passwordVisible = !passwordVisible;

        // Sincronizar contraseñas en ambos campos
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
        } else {
            passwordField.setText(visiblePasswordField.getText());
        }

        // Cambiar visibilidad
        imgEye.setImage(passwordVisible ? imgEyeClosed : imgEyeOpen);
        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);
        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
    }
}