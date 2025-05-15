package com.mpm.speakupdesk.controller.modulo;

import com.mpm.speakupdesk.dto.response.ProfesorSimpleResponseDTO;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.service.CursoService;
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

public class ProfesoresModuloController {

    @FXML private Label lblNombreCurso;
    @FXML private ListView<ProfesorSimpleResponseDTO> listaProfesores;

    private Stage stage;
    private Curso curso;
    private ObservableList<ProfesorSimpleResponseDTO> profesoresData = FXCollections.observableArrayList();


    public void initialize(){
        listaProfesores.setItems(profesoresData);
        listaProfesores.setCellFactory(param -> new UsuarioListCell());
    }

    public void initData(Curso curso) {
        this.curso = curso;
        lblNombreCurso.setText("Profesores de: " + curso.getNombre());
        //cargar profesores del curso
        cargarProfesoresDeCurso(curso.getId());
    }

    private void cargarProfesoresDeCurso(Long cursoId) {
        CursoService.listarProfesoresDelCurso(cursoId)
                .thenAccept(profesores -> {
                    Platform.runLater(() -> {
                        //System.out.println("Profesores recibidos: " + profesores.size()); // ✅ Log frontend
                        //profesores.forEach(p -> System.out.println(p.getNombreCompleto())); // ✅ Log de cada profesor

                        profesoresData.clear();
                        if (profesores.isEmpty()) {
                            profesoresData.add(new ProfesorSimpleResponseDTO(null, "Sin profesores registrados", ""));
                        } else {
                            profesores.sort(Comparator.comparing(
                                    ProfesorSimpleResponseDTO::getNombreCompleto,
                                    String.CASE_INSENSITIVE_ORDER
                            ));
                            profesoresData.addAll(profesores);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        //System.err.println("Error al cargar profesores: " + ex.getMessage()); // ✅ Log de error
                        profesoresData.clear();
                        profesoresData.add(new ProfesorSimpleResponseDTO(null, "Error al cargar datos", ""));
                    });
                    return null;
                });
    }

    private static class UsuarioListCell extends ListCell<ProfesorSimpleResponseDTO>{
        private final HBox content = new HBox();;
        private final Label nombre = new Label();;
        private final Label email = new Label();;
        private final Region spacer = new Region();;

        public UsuarioListCell() {
            //configuracion de estilos
            content.setSpacing(10);
            content.setPadding(new Insets(5,10,5,10));
            content.setAlignment(Pos.CENTER_LEFT);
            //el spacer ocuipara el espacio disponible
            HBox.setHgrow(spacer, Priority.ALWAYS);
            //añadir componentes al HBox
            content.getChildren().addAll(nombre,spacer, email);
            //establecer el estilo del nombre e email
            nombre.setStyle("-fx-font-size: 14px;");
            email.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        }
        @Override
        protected void updateItem(ProfesorSimpleResponseDTO profesor, boolean empty) {
            super.updateItem(profesor, empty);
            if (empty || profesor == null) {
                setGraphic(null);
            } else {
                nombre.setText(profesor.getNombreCompleto()); // Usar nombreCompleto
                email.setText(profesor.getEmail());
                setGraphic(content);
            }
        }
    }


    @FXML
    public void cerrarModal(ActionEvent actionEvent) {
        if(stage != null){
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
