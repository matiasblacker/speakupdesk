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
        // Añadir listener para detectar cambios en la contraseña
        pfNuevaContrasena.textProperty().addListener((observable, oldValue, newValue) -> {
            cambiarPassword = !newValue.isEmpty();
        });
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

        System.out.println("Campo de contraseña: " + (pfNuevaContrasena.getText().isEmpty() ? "vacío" : "con valor"));
        System.out.println("Variable cambiarPassword: " + cambiarPassword);

        // Solo enviar password si se cambia
        if (!pfNuevaContrasena.getText().isEmpty()) {
            int longitud = pfNuevaContrasena.getLength();

            if (longitud < 8 || longitud > 16) {
                CustomAlerts.mostrarAdvertencia("La contraseña debe tener entre 8 y 16 caracteres.");
                return; // <- Esto evita seguir si la contraseña es inválida
            }

            //System.out.println("Actualizando contraseña para usuario: " + usuarioActualizado.getEmail());
            usuarioActualizado.setPassword(pfNuevaContrasena.getText());
        }

        System.out.println("Datos a enviar: " + usuarioActualizado);
        UsuarioService.update(usuarioActualizado)
                .thenAcceptAsync(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Usuario actualizado con éxito");
                    //System.out.println("Usuario actualizado correctamente");
                    onUpdate.run();
                    stage.close();
                }))
                .exceptionally(e -> {
                    System.err.println("Error al actualizar usuario: " + e.getCause().getMessage());
                    Platform.runLater(() -> {
                        String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        // Verificar si es un error de email duplicado
                        if (errorMsg.contains("correo electrónico ya está registrado") ||
                                errorMsg.contains("email ya está registrado")) {
                            CustomAlerts.mostrarAdvertencia("El email ingresado ya existe en el sistema. Por favor, utilice otro.");
                        } else {
                            CustomAlerts.mostrarError("Error: " + errorMsg);
                        }
                    });
                    return null;
                });
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isEmpty() ||
                txtApellido.getText().isEmpty() ||
                txtEmail.getText().isEmpty()) {

            CustomAlerts.mostrarAdvertencia("Por favor completa todos los campos obligatorios.");
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
