package com.mpm.speakupdesk.controller.alumno;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Alumno;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.service.AlumnoService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static com.mpm.speakupdesk.commonutils.CustomAlerts.mostrarError;

public class AlumnoController {
    //componentes de la UI
    private TableView<Alumno> alumnosTable;
    private TableColumn<Alumno, Long> colIdAlumno;
    private TableColumn<Alumno, String> colNombreAlumno;
    private TableColumn<Alumno, String> colNumeroLista;
    private TableColumn<Alumno, String> colModulo;
    private Pagination pagination;

    //componentes de usuario
    private Long colegioId;
    private LoginResponse usuarioLogueado;

    //Datos y estado
    private ObservableList<Alumno> alumnosData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;

    public AlumnoController(TableView<Alumno> alumnosTable,
                            TableColumn<Alumno, Long> colIdAlumno,
                            TableColumn<Alumno, String> colNombreAlumno,
                            TableColumn<Alumno, String> colNumeroLista,
                            TableColumn<Alumno, String> colModulo,
                            Pagination pagination) {
        this.alumnosTable = alumnosTable;
        this.colIdAlumno = colIdAlumno;
        this.colNombreAlumno = colNombreAlumno;
        this.colNumeroLista = colNumeroLista;
        this.colModulo = colModulo;
        this.pagination = pagination;

        inicializarTablaAlumnos();

    }

    public void setUsuarioLogueado(LoginResponse usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        // Manejar colegioId solo si es ADMIN_COLEGIO
        if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO && usuarioLogueado.getColegioId() != null) {
            this.colegioId = usuarioLogueado.getColegioId().longValue();
        }
    }

    public void loadAlumnos(){  //para cargar todos los alumnos en la tabla
        CompletableFuture<List<Alumno>> cargaAlumnos = AlumnoService.findAll();
        cargaAlumnos.thenAccept(alumnos ->{
            Platform.runLater(()->{
                alumnosData.setAll(alumnos);
                alumnosTable.setItems(alumnosData);
                configurePagination(alumnos.size());
            });
        }).exceptionally(e ->{
            Platform.runLater(() -> mostrarError("Error: " + e.getCause().getMessage()));
            return null;
        });
    }

    /*public void loadAlumnosByCurso(Long cursoId){  //para cargar los alumno del curso
        CompletableFuture<List<Alumno>> cargaAlumnos = AlumnoService.findByCursoId(cursoId);
        cargaAlumnos.thenAccept(alumnos ->{
            Platform.runLater(()->{
                alumnosData.setAll(alumnos);
                alumnosTable.setItems(alumnosData);
                configurePagination(alumnos.size());
            });
        }).exceptionally(e ->{
            Platform.runLater(() -> mostrarError("Error: " + e.getCause().getMessage()));
            return null;
        });
    }*/


    private void configurePagination(int totalItems) {
        int pageCount = (totalItems + itemsPerPage - 1) / itemsPerPage;
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount); // Evitar 0 páginas
        pagination.setCurrentPageIndex(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                updateTablePage(newIndex.intValue())
        );
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, alumnosData.size());
        //crea un observable para la tabla actual
        ObservableList<Alumno> pageData = FXCollections.observableArrayList(
                alumnosData.subList(fromIndex, toIndex)
        );
        alumnosTable.setItems(pageData);
        // refrescar la tabla
        alumnosTable.refresh();
    }

    private void inicializarTablaAlumnos() {
        colIdAlumno.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNumeroLista.setCellValueFactory(new PropertyValueFactory<>("numeroLista"));
        colModulo.setCellValueFactory(new PropertyValueFactory<>("nombreCurso"));

        addActionColumn();
    }

    private void addActionColumn() {
        // Eliminar cualquier columna de opciones existente primero
        alumnosTable.getColumns().removeIf(col -> "Opciones".equals(col.getText()));
        TableColumn<Alumno, Void> colOpciones = new TableColumn<>("Opciones");
        colOpciones.setCellFactory(param -> new TableCell<Alumno, Void>() {
            private Button btnOpciones;
            private ContextMenu contextMenu;
            {
                // Inicializar el botón y el menú contextual una vez
                btnOpciones = new Button("⋮");
                contextMenu = new ContextMenu();
                MenuItem editarItem = new MenuItem("Editar");
                MenuItem eliminarItem = new MenuItem("Eliminar");

                contextMenu.getItems().addAll(editarItem, eliminarItem);
                // Estilos y configuración del botón
                btnOpciones.getStyleClass().add("btn-opciones-celda");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Limpiar cualquier contenido previo
                setGraphic(null);
                setText(null);
                // Solo crear botón si hay un elemento válido
                if (!empty && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                    Alumno alumno = getTableView().getItems().get(getIndex());
                    if (alumno != null) {
                        // El menú contextual tiene índices 0 y 1, no 1 y 2
                        contextMenu.getItems().get(0).setOnAction(event -> editarAlumno(alumno));
                        contextMenu.getItems().get(1).setOnAction(event -> eliminarAlumno(alumno));

                        btnOpciones.setOnAction(e ->
                                contextMenu.show(btnOpciones, Side.BOTTOM, 0, 0)
                        );
                        setGraphic(btnOpciones);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });
        // Añadir la columna al final
        alumnosTable.getColumns().add(colOpciones);
    }

    public void abrirModalCrearAlumno(Stage parentStage){
        try{
            if (usuarioLogueado == null || usuarioLogueado.getRol() != Rol.ADMIN_COLEGIO) {
                CustomAlerts.mostrarError("Solo los administradores de colegio pueden crear alumnos");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/alumno/crear_alumno.fxml"));
            Parent root = loader.load();

            CrearAlumnoController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Alumno");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if(controller.isGuardado()){
                String[] datos = controller.getDatosAlumno();
                //logica de guardado
                Alumno nuevoAlumno = new Alumno();
                nuevoAlumno.setNombre(datos[0]);
                nuevoAlumno.setApellido(datos[1]);
                nuevoAlumno.setNumeroLista(Integer.valueOf(datos[2]));
                nuevoAlumno.setCursoId(Long.valueOf(datos[3]));

                AlumnoService.save(nuevoAlumno)
                        .thenAccept(cursoGuardado ->{
                            Platform.runLater(()->{
                                CustomAlerts.mostrarExito("Alumno creado exitosamente");
                                loadAlumnos();
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() ->
                                    CustomAlerts.mostrarError("Error: " + ex.getCause().getMessage())
                            );
                            return null;
                        });

            }
        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void eliminarAlumno(Alumno alumno) {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Eliminar Alumno",
                "¿Estás seguro de eliminar este Alumno?\n");
        if (confirmacion){
            AlumnoService.delete(alumno.getId())
                    .thenAcceptAsync(v -> Platform.runLater(() ->{
                        alumnosData.remove(alumno);
                        CustomAlerts.mostrarExito("Alumno eliminado");
                        configurePagination(alumnosData.size());
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() ->
                                CustomAlerts.mostrarError("Error: " + ex.getCause().getMessage())
                        );
                        return null;
                    });
        }
    }

    private void editarAlumno(Alumno alumno) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/alumno/editar_alumno.fxml"));
            Parent root = loader.load();

            EditarAlumnoController controller= loader.getController();
            controller.initData(alumno, this::loadAlumnos);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Alumno");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setStage(stage);
            stage.showAndWait();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el editor");
        }
    }


}
