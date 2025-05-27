package com.mpm.speakupdesk.controller.alumno;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Alumno;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.service.AlumnoService;
import com.mpm.speakupdesk.service.CursoService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.util.List;
import java.util.stream.Collectors;

public class EditarAlumnoController {

    @FXML private TextField txtNombreAlumno;
    @FXML private TextField txtApellidoAlumno;
    @FXML private TextField txtNumeroLista;
    @FXML private ComboBox<Curso> cbCurso;

    private Alumno alumno;
    private Stage stage;
    private Runnable onUpdate;

    public void initData(Alumno alumno, Runnable onUpdate) {
        this.alumno = alumno;
        this.onUpdate = onUpdate;

        // Establecer datos iniciales
        txtNombreAlumno.setText(alumno.getNombre());
        txtApellidoAlumno.setText(alumno.getApellido());
        txtNumeroLista.setText(String.valueOf(alumno.getNumeroLista()));

        // Cargar cursos y configurar ComboBox
        cargarCursos();
    }

    private void cargarCursos() {
        CursoService.findByColegioId()
                .thenAccept(cursos -> {
                    Platform.runLater(() -> {
                        // Filtrar curso especial y convertir a ObservableList
                        List<Curso> cursosFiltrados = cursos.stream()
                                .filter(curso -> !curso.getNombre().equalsIgnoreCase("Sin curso asignado"))
                                .collect(Collectors.toList());

                        cbCurso.getItems().setAll(cursosFiltrados);

                        // Preseleccionar curso actual del alumno
                        if (alumno.getCursoId() != null) {
                            cursosFiltrados.stream()
                                    .filter(c -> c.getId().equals(alumno.getCursoId()))
                                    .findFirst()
                                    .ifPresent(c -> cbCurso.getSelectionModel().select(c));
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al cargar cursos: " + ex.getCause().getMessage())
                    );
                    return null;
                });
    }

    @FXML
    private void editarAlumno(ActionEvent actionEvent) {
        if (!validarCampos()) return;

        // Actualizar datos del alumno
        alumno.setNombre(txtNombreAlumno.getText());
        alumno.setApellido(txtApellidoAlumno.getText());
        alumno.setNumeroLista(Integer.parseInt(txtNumeroLista.getText()));

        // Actualizar curso (si se seleccionó uno)
        Curso cursoSeleccionado = cbCurso.getSelectionModel().getSelectedItem();
        alumno.setCursoId(cursoSeleccionado != null ? cursoSeleccionado.getId() : null);

        // Guardar cambios
        AlumnoService.update(alumno)
                .thenAccept(updated -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Alumno actualizado");
                    stage.close();
                    if (onUpdate != null) onUpdate.run();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al actualizar: " + ex.getCause().getMessage())
                    );
                    return null;
                });
    }

    private boolean validarCampos() {
        // Validar campos requeridos
        if (txtNombreAlumno.getText().isEmpty() ||
                txtApellidoAlumno.getText().isEmpty() ||
                txtNumeroLista.getText().isEmpty()) {

            CustomAlerts.mostrarAdvertencia("Complete todos los campos obligatorios");
            return false;
        }

        // Validar número de lista
        try {
            Integer.parseInt(txtNumeroLista.getText());
        } catch (NumberFormatException e) {
            CustomAlerts.mostrarAdvertencia("Número de lista inválido");
            return false;
        }

        return true;
    }

    @FXML
    private void cancelar(ActionEvent actionEvent) {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}