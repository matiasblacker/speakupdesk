package com.mpm.speakupdesk.controller.usuario;

import com.mpm.speakupdesk.dto.response.LoginResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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

    }
}
