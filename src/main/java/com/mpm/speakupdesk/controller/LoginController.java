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
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    private boolean passwordVisible = false;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;
    @FXML private ImageView imgEye;

    private Image imgEyeOpen = new Image(getClass().getResourceAsStream("/img/eye.png"));
    private Image imgEyeClosed = new Image(getClass().getResourceAsStream("/img/eyeslash.png"));

    @FXML
    public void initialize() {
        imgEye.setImage(imgEyeOpen);
        // Configurar los elementos de alerta en CustomAlerts
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
    }

    @FXML
    private void handleLogin() {
        new Thread(() -> {
            boolean success = AuthService.login(emailField.getText(), passwordField.getText());
            boolean success2 = AuthService.login(emailField.getText(), visiblePasswordField.getText());

            Platform.runLater(() -> {
                if (success || success2) {
                    LoginResponse usuario = AuthService.getUsuarioLogueado();
                    if (usuario == null) {
                        CustomAlerts.mostrarError("Error al obtener datos del usuario");
                        return;
                    }
                    // Obtener el rol desde LoginResponse ✅
                    Rol rol = usuario.getRol();

                    String fxmlPath;
                    // Asignar la vista correspondiente según el rol
                    switch (rol) {
                        case ADMIN_GLOBAL -> fxmlPath = "/views/dashboard/admin.fxml";
                        case ADMIN_COLEGIO -> fxmlPath = "/views/dashboard/admin.fxml";
                        default -> {
                            CustomAlerts.mostrarError("Rol no válido: " + rol);
                            return;
                        }
                    }
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                        Parent root = loader.load();

                        Stage newStage = new Stage();
                        newStage.setScene(new Scene(root));
                        newStage.setMaximized(true);
                        newStage.setTitle("SpeakUP! - " + rol);
                        newStage.show();

                        // Cerrar la ventana de login
                        Stage loginStage = (Stage) emailField.getScene().getWindow();
                        loginStage.close();

                        // Inyectar datos del usuario en el controlador de la vista
                        if (rol == Rol.ADMIN_GLOBAL || rol == Rol.ADMIN_COLEGIO) {
                            AdminController controller = loader.getController();
                            controller.initData(usuario);
                        }
                    } catch (IOException e) {
                        CustomAlerts.mostrarError("Error al cargar la interfaz: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    CustomAlerts.mostrarError("Credenciales incorrectas");
                }
            });
        }).start();
    }

    @FXML
    public void togglePasswordVisibility(ActionEvent actionEvent) {
        passwordVisible = !passwordVisible;

        // Cambiar la imagen según el estado
        if (passwordVisible) {
            imgEye.setImage(imgEyeClosed); // Ojo cerrado
        } else {
            imgEye.setImage(imgEyeOpen); // Ojo abierto
        }

        if (passwordVisible) {
            String password = passwordField.getText();
            visiblePasswordField.setText(password);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {
            String password = visiblePasswordField.getText();
            passwordField.setText(password);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        }
    }

}