package com.mpm.speakupdesk.controller.instituto;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Colegio;
import com.mpm.speakupdesk.service.ColegioService;
import com.mpm.speakupdesk.service.ComunaService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.List;

public class EditarInstitutoController {

    @FXML private TextField txtNombreInstituto;
    @FXML private ComboBox<String> comboRegion;
    @FXML private ComboBox<String> comboComuna;

    private Colegio colegio;
    private Stage stage;
    private Runnable onUpdate;
    private ComunaService comunaService = new ComunaService(); // Inyectar servicio

    public void initialize() {
        // Cargar regiones al inicializar el controlador
        cargarRegiones();
        // En lugar de usar un ListChangeListener, podemos usar un task para inicializar
        comboRegion.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarComunas(newVal);
            }
        });
    }

    public void initData(Colegio colegio, Runnable onUpdate) {
        this.colegio = colegio;
        this.onUpdate = onUpdate;
        // Establecer el nombre del instituto
        txtNombreInstituto.setText(colegio.getNombre());
        // Usar Platform.runLater para asegurar que la UI está lista
        Platform.runLater(() -> {
            // Seleccionar la región
            if (colegio.getRegion() != null &&
                    comboRegion.getItems().contains(colegio.getRegion())) {
                comboRegion.setValue(colegio.getRegion());
                // La comuna se cargará cuando se dispare el listener de valueProperty
            }
        });
    }

    private void cargarRegiones() {
        ObservableList<String> regiones = comunaService.getNombresRegiones();
        comboRegion.setItems(regiones);
    }

    private void actualizarComunas(String region) {
        if (region != null) {
            // Obtener las comunas para la región seleccionada
            List<String> comunas = comunaService.filtrarComunas(region, "");
            comboComuna.setItems(FXCollections.observableArrayList(comunas));

            // Seleccionar la comuna del colegio si existe
            if (colegio != null && colegio.getComuna() != null) {
                String comunaColegio = colegio.getComuna();
                // Verificar si la comuna está en la lista antes de seleccionarla
                if (comunas.contains(comunaColegio)) {
                    Platform.runLater(() -> comboComuna.setValue(comunaColegio));
                }
            }
        }
    }

    @FXML
    private void guardarCambios() {
        if (!validarCampos()) return;

        Colegio colegioActualizado = new Colegio();
        colegioActualizado.setId(colegio.getId());
        colegioActualizado.setNombre(txtNombreInstituto.getText());
        colegioActualizado.setRegion(comboRegion.getValue());
        colegioActualizado.setComuna(comboComuna.getValue());

        ColegioService.update(colegioActualizado)
                .thenAcceptAsync(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Colegio actualizado con éxito");
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

    private boolean validarCampos() {
        String nombreInstituto = txtNombreInstituto.getText();
        String region = comboRegion.getValue();
        String comuna = comboComuna.getValue();

        if (nombreInstituto == null || nombreInstituto.isEmpty() ||
                region == null ||
                comuna == null) {

            CustomAlerts.mostrarAdvertencia("Todos los campos son obligatorios");
            return false;
        }
        return true;
    }

    @FXML
    public void cancelar(ActionEvent actionEvent) {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
