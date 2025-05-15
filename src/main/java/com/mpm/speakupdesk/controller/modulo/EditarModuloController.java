package com.mpm.speakupdesk.controller.modulo;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.service.CursoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditarModuloController {
    @FXML private TextField txtNombreModulo;

    private Curso curso;
    private Stage stage;
    private Runnable onUpdate;

    public void initData(Curso curso, Runnable onUpdate){
        this.curso = curso;
        this.onUpdate = onUpdate;
        //establecer nombre del curso
        txtNombreModulo.setText(curso.getNombre());
    }

    @FXML
    private void editarModulo() {
        if(!validarCampos()) return;

        Curso cursoActualizado = new Curso();
        cursoActualizado.setId(curso.getId());
        cursoActualizado.setNombre(txtNombreModulo.getText());

        CursoService.update(cursoActualizado)
                .thenAcceptAsync(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Módulo actualizado con éxito");
                    onUpdate.run();
                    stage.close();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al actualizar: " + e)
                    );
                    return null;
                });
    }

    private boolean validarCampos(){
        String nombreModulo = txtNombreModulo.getText();
        if (nombreModulo == null){
            CustomAlerts.mostrarAdvertencia("El campo es obligatorio");
        }
        return true;
    }

    @FXML
    public void cancelar(ActionEvent actionEvent) {
        stage.close();
    }

    public void setStage(Stage stage){
        this. stage = stage;
    }
}
