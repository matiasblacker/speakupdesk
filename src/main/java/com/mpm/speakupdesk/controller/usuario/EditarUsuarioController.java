package com.mpm.speakupdesk.controller.usuario;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Usuario;
import com.mpm.speakupdesk.service.AuthService;
import com.mpm.speakupdesk.service.UsuarioService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditarUsuarioController {
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cbEstado;
    @FXML private PasswordField pfNuevaContrasena;

    private boolean esAutoEdicion; // Indica si el usuario edita su propio perfil
    private boolean cambiarPassword = false;
    private Usuario usuario;
    private Stage stage;
    private Runnable onUpdate;

    public void initialize() {
        // Configurar ComboBox
        cbEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
    }

    public void initData(Usuario usuario, Runnable onUpdate) {
        this.usuario = usuario;
        this.onUpdate = onUpdate;

        // Cargar datos del usuario
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtEmail.setText(usuario.getEmail());
        cbEstado.getSelectionModel().select(usuario.isEnabled() ? "Activo" : "Inactivo");

        // Verificar si es auto-edición usando AuthService existente
        LoginResponse usuarioLogueado = AuthService.getUsuarioLogueado();
        esAutoEdicion = usuario.getId().equals(usuarioLogueado.getId());

        // Deshabilitar el campo de estado si es auto-edición
        if (esAutoEdicion) {
            cbEstado.setDisable(true);
        }
    }

    @FXML
    private void guardarCambios() {
        if (!validarCampos()) return;
        
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setId(usuario.getId());
        usuarioActualizado.setNombre(txtNombre.getText());
        usuarioActualizado.setApellido(txtApellido.getText());
        usuarioActualizado.setEmail(txtEmail.getText());
        usuarioActualizado.setEnabled(cbEstado.getValue().equals("Activo"));
        // Solo enviar password si se cambia
        if (cambiarPassword) {
            usuarioActualizado.setPassword(pfNuevaContrasena.getText());
        }
        UsuarioService.update(usuarioActualizado)
                .thenAcceptAsync(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Usuario actualizado con éxito");
                    onUpdate.run();
                    stage.close();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error: " + e.getCause().getMessage())
                    );
                    return null;
                });
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() ||
                txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty()) {

            CustomAlerts.mostrarError("Campos obligatorios vacíos");
            return false;
        }
        return true;
    }

    @FXML
    private void cancelar() {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
