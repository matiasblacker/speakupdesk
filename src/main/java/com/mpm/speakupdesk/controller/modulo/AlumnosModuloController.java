package com.mpm.speakupdesk.controller.modulo;

import com.mpm.speakupdesk.model.Alumno;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.service.AlumnoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.Comparator;

public class AlumnosModuloController {
    @FXML private Label lblNombreCurso;
    @FXML private ListView<Alumno> listaAlumnos;

    private Stage stage;
    private Curso curso;
    private ObservableList<Alumno> alumnosData = FXCollections.observableArrayList();

    public void initialize() {
        listaAlumnos.setItems(alumnosData);

        // Configurar el cell factory personalizado
        listaAlumnos.setCellFactory(param -> new AlumnoListCell());
    }

    public void initData(Curso curso) {
        this.curso = curso;
        lblNombreCurso.setText("Alumnos de : " + curso.getNombre());

        // Cargar los alumnos del curso
        cargarAlumnosDeCurso(curso.getId());
    }

    private void cargarAlumnosDeCurso(Long cursoId) {
        // Llamar al servicio para obtener los alumnos del curso
        AlumnoService.findByCursoId(cursoId)
                .thenAccept(alumnos -> {
                    Platform.runLater(() -> {
                        alumnosData.clear();
                        if (alumnos.isEmpty()) {
                            // Si no hay alumnos, mostrar un mensaje
                            Alumno noAlumnos = new Alumno();
                            noAlumnos.setNombre("Sin alumnos registrados");
                            noAlumnos.setApellido("");
                            noAlumnos.setNumeroLista(0);
                            alumnosData.add(noAlumnos);
                        } else {
                            alumnos.sort(Comparator.comparingInt(Alumno::getNumeroLista));
                            alumnosData.addAll(alumnos);
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        alumnosData.clear();
                        Alumno error = new Alumno();
                        error.setNombre("Error al cargar los alumnos: " + e.getMessage());
                        error.setNumeroLista(0);
                        alumnosData.add(error);
                    });
                    return null;
                });
    }

    @FXML
    public void cerrarModal(ActionEvent actionEvent) {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Clase para personalizar cómo se muestra cada celda en el ListView
    private static class AlumnoListCell extends ListCell<Alumno> {
        private final HBox content;
        private final Label nombre;
        private final Label numero;
        private final Region spacer;

        public AlumnoListCell() {
            content = new HBox();
            nombre = new Label();
            numero = new Label();
            spacer = new Region();

            // Configuración de estilos
            content.setSpacing(10);
            content.setPadding(new Insets(5, 10, 5, 10));
            content.setAlignment(Pos.CENTER_LEFT);

            // El spacer ocupará
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Añadir componentes al HBox
            content.getChildren().addAll(nombre, spacer, numero);

            // Establecer el estilo del nombre y número
            nombre.setStyle("-fx-font-size: 14px;");
            numero.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        }

        @Override
        protected void updateItem(Alumno alumno, boolean empty) {
            super.updateItem(alumno, empty);

            if (empty || alumno == null) {
                setGraphic(null);
            } else {
                nombre.setText(alumno.getNombreCompleto());
                // Solo mostrar número de lista si es mayor que 0
                if (alumno.getNumeroLista() > 0) {
                    numero.setText("#" + alumno.getNumeroLista());
                } else {
                    numero.setText("");
                }
                setGraphic(content);
            }
        }
    }
}