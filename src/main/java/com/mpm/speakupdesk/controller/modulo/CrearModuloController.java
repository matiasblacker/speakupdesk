package com.mpm.speakupdesk.controller.modulo;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrearModuloController {

    @FXML TextField txtNombreModulo;
    private Stage dialogStage;
    private boolean guardado;

    @FXML
    public void initialize(){
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public boolean isGuardado(){
        return guardado;
    }

    public String[]getDatosModulo(){
        return new String[]{
                txtNombreModulo.getText().trim()
        };
    }

    public void cancelar(ActionEvent actionEvent) {
        dialogStage.close();
    }

    public void crearModulo(ActionEvent actionEvent) {
        if(validarDatos()){
            guardado = true;
           // CustomAlerts.mostrarExito("Módulo creado exitosamente");
            dialogStage.close();
        }
    }

    private boolean validarDatos(){
        if(txtNombreModulo.getText().isEmpty()){
            CustomAlerts.mostrarError("Nombre del módulo es obligatorio");
            return false;
        }
        return true;
    }
}
