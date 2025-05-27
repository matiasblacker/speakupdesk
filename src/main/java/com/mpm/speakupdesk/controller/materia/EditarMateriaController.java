package com.mpm.speakupdesk.controller.materia;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Materia;
import com.mpm.speakupdesk.service.MateriaService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class EditarMateriaController {
    @FXML private TextField txtNombreMateria;

    private Materia materia;
    private Stage stage;
    private Runnable onUpdate;

    public void initData(Materia materia, Runnable onUpdate) {
        this.materia = materia;
        this.onUpdate = onUpdate;
        // Cargar el nombre de la materia en el TextField
        txtNombreMateria.setText(materia.getNombre()); // ✅ Añade esta línea
    }

    @FXML
    private void editarMateria() {
        if(!validarCampos()) return;

        Materia materiaActualizada = new Materia();
        materiaActualizada.setId(materia.getId());
        materiaActualizada.setNombre(txtNombreMateria.getText());

        MateriaService.update(materiaActualizada)
                .thenAcceptAsync(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Materia actualizada con éxito");
                    onUpdate.run();
                    stage.close();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        String errorMsg = "Error al actualizar: ";
                        if (e.getCause() instanceof IOException) {
                            errorMsg += "No se pudo conectar al servidor.";
                        } else {
                            errorMsg += e.getCause().getMessage();
                        }
                        CustomAlerts.mostrarError(errorMsg); // ✅ Mensaje detallado
                    });
                    return null;
                });
    }

    private boolean validarCampos(){
        String nombreMateria = txtNombreMateria.getText();
        if (nombreMateria == null){
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
