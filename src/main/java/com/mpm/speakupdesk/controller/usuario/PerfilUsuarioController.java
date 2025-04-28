package com.mpm.speakupdesk.controller.usuario;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.controller.AdminController;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Usuario;
import com.mpm.speakupdesk.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PerfilUsuarioController {
    @FXML
    private Label lblNombreCompleto;
    @FXML private Label lblEmail;
    @FXML private Label lblRol;
    @FXML private Label lblColegio;
    private Stage stage;

    public void initData(LoginResponse usuario) {
        lblNombreCompleto.setText("Nombre: " + usuario.getNombre() + " " + usuario.getApellido());
        lblEmail.setText("Email: " + usuario.getEmail());
        lblRol.setText("Rol: " + usuario.getRol().name());
        lblColegio.setText("Colegio : " + usuario.getNombreColegio());
    }
    @FXML
    private void cerrarModal() {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void actualizarPerfil(ActionEvent actionEvent) {
        try {
            // Obtener el usuario actual desde AuthService
            LoginResponse usuarioActual = AuthService.getUsuarioLogueado();

            // Crear un objeto Usuario con los datos del LoginResponse
            Usuario usuario = new Usuario();
            usuario.setId(usuarioActual.getId());
            usuario.setNombre(usuarioActual.getNombre());
            usuario.setApellido(usuarioActual.getApellido());
            usuario.setEmail(usuarioActual.getEmail());
            usuario.setEnabled(true); // Asumimos que está activo ya que puede iniciar sesión

            // Cargar el formulario de edición
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/editar_usuario.fxml"));
            Parent root = loader.load();

            // Configurar el controlador
            EditarUsuarioController controller = loader.getController();
            // El usuario se actualizará cuando se cierre el modal
            controller.initData(usuario, () -> {
                // Actualizar los datos en el perfil cuando se completa la edición
                LoginResponse actualizado = AuthService.getUsuarioLogueado();
                initData(actualizado);
            });

            // Configurar y mostrar el modal
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Editar Perfil");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            // Pasar la referencia del stage al controlador
            controller.setStage(modalStage);
            modalStage.showAndWait();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al abrir el editor de perfil: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
