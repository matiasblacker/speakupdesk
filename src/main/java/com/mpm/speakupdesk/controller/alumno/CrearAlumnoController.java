package com.mpm.speakupdesk.controller.alumno;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.service.CursoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;
import java.util.stream.Collectors;

public class CrearAlumnoController {

    @FXML private TextField txtNombreAlumno;
    @FXML private TextField txtApellidoAlumno;
    @FXML private TextField txtNumeroLista;
    @FXML private ComboBox<Curso> cbCurso;

    private Stage dialogStage;
    private boolean guardado = false;
    private String[] datosAlumno;

    @FXML
    public void initialize() {
        cargarCursos();
        // Configurar el StringConverter para mostrar solo el nombre del curso
        cbCurso.setConverter(new StringConverter<Curso>() {
            @Override
            public String toString(Curso curso) {
                return curso != null ? curso.getNombre() : "";
            }

            @Override
            public Curso fromString(String string) {
                return cbCurso.getItems().stream()
                        .filter(curso -> curso.getNombre().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void cargarCursos() {
        ObservableList<Curso> cursos = FXCollections.observableArrayList();
        // Obtenemos la lista de cursos del colegio actual
        CursoService.findByColegioId()
                .thenAccept(listaCursos -> {
                    Platform.runLater(() -> {
                        // Filtrar el curso especial
                        List<Curso> cursosFiltrados = listaCursos.stream()
                                .filter(curso ->
                                        !curso.getNombre().equalsIgnoreCase("Sin curso asignado")
                                )
                                .collect(Collectors.toList());
                        cursos.addAll(cursosFiltrados);
                        cbCurso.setItems(cursos);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al cargar los cursos: " + ex.getCause().getMessage())
                    );
                    return null;
                });
    }
    private boolean validarCampos() {
        StringBuilder errores = new StringBuilder();

        if (txtNombreAlumno.getText().trim().isEmpty()) {
            errores.append("El nombre del alumno es obligatorio\n");
        }

        if (txtApellidoAlumno.getText().trim().isEmpty()) {
            errores.append("El apellido del alumno es obligatorio\n");
        }

        if (txtNumeroLista.getText().trim().isEmpty()) {
            errores.append("El número de lista es obligatorio\n");
        } else {
            try {
                Integer.parseInt(txtNumeroLista.getText().trim());
            } catch (NumberFormatException e) {
                errores.append("El número de lista debe ser un valor numérico\n");
            }
        }

        if (cbCurso.getValue() == null) {
            errores.append("Debe seleccionar un curso\n");
        }

        if (errores.length() > 0) {
            CustomAlerts.mostrarAdvertencia("Campos incompletos");
            return false;
        }

        return true;
    }
    @FXML
    public void crearAlumno(ActionEvent actionEvent) {
        if (validarCampos()) {
            // Obtenemos los datos del formulario
            String nombre = txtNombreAlumno.getText().trim();
            String apellido = txtApellidoAlumno.getText().trim();
            String numeroLista = txtNumeroLista.getText().trim();

            Curso cursoSeleccionado = cbCurso.getValue();
            if (cursoSeleccionado == null) {
                CustomAlerts.mostrarAdvertencia("Debe seleccionar un curso");
                return;
            }

            // Guardamos los datos en un array para devolver al controller principal
            datosAlumno = new String[4];
            datosAlumno[0] = nombre;
            datosAlumno[1] = apellido;
            datosAlumno[2] = numeroLista;
            datosAlumno[3] = String.valueOf(cursoSeleccionado.getId());

            guardado = true;
            dialogStage.close();
        }
    }

    @FXML
    public void cancelar(ActionEvent actionEvent) {
        guardado = false;
        dialogStage.close();
    }
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isGuardado() {
        return guardado;
    }

    public String[] getDatosAlumno() {
        return datosAlumno;
    }
}
