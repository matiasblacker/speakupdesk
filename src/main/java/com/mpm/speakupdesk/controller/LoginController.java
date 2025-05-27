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
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;
    @FXML private ImageView imgEye;
    @FXML private Button loginButton; // Asegúrate de tener esta referencia en tu FXML
    @FXML private CheckBox rememberUserCheckBox;
    @FXML private Button forgotPass;

    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private boolean passwordVisible = false;
    private Image imgEyeOpen = new Image(getClass().getResourceAsStream("/img/eye.png"));
    private Image imgEyeClosed = new Image(getClass().getResourceAsStream("/img/eyeslash.png"));

    @FXML
    public void initialize() {
        imgEye.setImage(imgEyeOpen);
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
        visiblePasswordField.setManaged(false); // Ocultar inicialmente el campo visible

        // Cargar email guardado
        String savedEmail = prefs.get("rememberedEmail", "");
        String savedPass = prefs.get("rememberedPass", "");
        if (!savedEmail.isEmpty() && !savedPass.isEmpty()) {
            emailField.setText(savedEmail);
            passwordField.setText(savedPass);
            rememberUserCheckBox.setSelected(true);
        }
    }

    //recuperacion contraseña
    @FXML
    private void handleRecoverPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/recovery.fxml"));
            Parent root = loader.load();

            RecoveryController controller = loader.getController();
            Stage recoveryStage = new Stage();

            // Configurar la ventana de recuperación
            recoveryStage.initModality(Modality.APPLICATION_MODAL);
            recoveryStage.setTitle("Recuperar contraseña - SpeakUP!");
            recoveryStage.setResizable(false);
            recoveryStage.setScene(new Scene(root));

            controller.setStage(recoveryStage);
            Stage loginStage = (Stage) emailField.getScene().getWindow();
            recoveryStage.show();
            recoveryStage.setOnCloseRequest(event -> {
                if (!loginStage.isShowing()) {
                    loginStage.show();
                }
            });
            loginStage.close();
            recoveryStage.centerOnScreen();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar la ventana de recuperación: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            CustomAlerts.mostrarError("Error inesperado al abrir la recuperación de contraseña");
            e.printStackTrace();
        }
    }

    //Login
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
                if (rememberUserCheckBox.isSelected()) {
                    prefs.put("rememberedEmail", emailField.getText());
                    prefs.put("rememberedPass", passwordField.getText());
                } else {
                    prefs.remove("rememberedEmail");
                    prefs.remove("rememberedPass");
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

    //envio de correo
    public void sendEmail(ActionEvent actionEvent) {
        new Thread(() -> {
            String correoDestino = "admi.speakup2024@gmail.com";
            String asunto = "Consulta";
            String cuerpo = "Hola, quiero contactarte...";

            String uriStr = String.format("https://mail.google.com/mail/?view=cm&fs=1&to=%s&su=%s&body=%s",
                    encode(correoDestino),
                    encode(asunto),
                    encode(cuerpo));

            openBrowserCrossPlatform(uriStr);
        }).start();
    }

    private String encode(String texto) {
        return texto.replace(" ", "%20")
                .replace("\n", "%0A")
                .replace("á", "%C3%A1")
                .replace("é", "%C3%A9")
                .replace("í", "%C3%AD")
                .replace("ó", "%C3%B3")
                .replace("ú", "%C3%BA");
        // Para una codificación más completa puedes usar URLEncoder.encode(...) con cuidado.
    }

    private void openBrowserCrossPlatform(String url) {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux / Unix
                Runtime.getRuntime().exec(new String[] { "xdg-open", url });
            } else {
                CustomAlerts.mostrarError("Sistema operativo no soportado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}