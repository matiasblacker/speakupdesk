package com.mpm.speakupdesk.controller;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.service.ComunaService;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrearInstitutoController {
    @FXML private TextField txtNombreInstituto;
    @FXML private ComboBox<String> comboRegion;
    @FXML private ComboBox<String> comboComuna;

    private final ComunaService comunaService = new ComunaService();
    private Stage dialogStage;
    private boolean guardado = false;

    @FXML
    public void initialize() {
        configurarAutocompletado();
        cargarRegiones();
    }

    private void configurarAutocompletado() {
        comboRegion.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarComunas(newVal);
            }
        });

        comboComuna.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (comboRegion.getValue() != null && !newVal.isEmpty()) {
                filtrarComunas(newVal);
            }
        });
    }

    private void cargarRegiones() {
        comboRegion.setItems(comunaService.getNombresRegiones());
    }

    private void actualizarComunas(String region) {
        ObservableList<String> comunas = comunaService.filtrarComunas(region, "");
        comboComuna.setItems(comunas);
        comboComuna.getEditor().clear();
    }

    private void filtrarComunas(String texto) {
        ObservableList<String> sugerencias = comunaService.filtrarComunas(
                comboRegion.getValue(),
                texto
        );
        comboComuna.setItems(sugerencias);
        comboComuna.show();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isGuardado() {
        return guardado;
    }

    public String[] getDatosInstituto() {
        return new String[]{
                txtNombreInstituto.getText().trim(),
                comboRegion.getValue(),
                comboComuna.getValue()
        };
    }

    @FXML
    private void cancelar(ActionEvent actionEvent) {
        dialogStage.close();
    }

    @FXML
    private void crearInstituto(ActionEvent actionEvent) {
        if (validarDatos()) {
            guardado = true;
            CustomAlerts.mostrarExito("Institución creada exitosamente");
            dialogStage.close();
        }
    }

    private boolean validarDatos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombreInstituto.getText().isBlank()) {
            errores.append("- Nombre es obligatorio\n");
        }
        if (comboRegion.getValue() == null) {
            errores.append("- Seleccione una región\n");
        }
        if (comboComuna.getValue() == null) {
            errores.append("- Seleccione una comuna\n");
        }

        if (errores.length() > 0) {
            CustomAlerts.mostrarError("Campos incompletos");
            return false;
        }
        return true;
    }


}