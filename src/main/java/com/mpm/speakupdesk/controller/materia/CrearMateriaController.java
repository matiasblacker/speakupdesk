package com.mpm.speakupdesk.controller.materia;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrearMateriaController {

    @FXML private TextField txtNombreMateria;
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

    public String[]getDatosMateria(){
        return new String[]{
                txtNombreMateria.getText().trim()
        };
    }

    public void cancelar(ActionEvent actionEvent) {
        dialogStage.close();
    }

    public void crearMateria(ActionEvent actionEvent) {
        if(validarDatos()){
            guardado = true;
            CustomAlerts.mostrarExito("Materia creada exitosamente");
            dialogStage.close();
        }
    }

    private boolean validarDatos(){
        if(txtNombreMateria.getText().isEmpty()){
            CustomAlerts.mostrarAdvertencia("Nombre de la materia es obligatoria");
            return false;
        }
        return true;
    }
}
