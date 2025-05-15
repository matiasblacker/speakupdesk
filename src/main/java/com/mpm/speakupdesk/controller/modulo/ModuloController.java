package com.mpm.speakupdesk.controller.modulo;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.service.CursoService;
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

public class ModuloController {
    //componentes de la UI
    private TableView<Curso> modulosTable;
    private TableColumn<Curso, Long> colIdModulo;
    private TableColumn<Curso, String> colNombreModulo;
    private Pagination pagination;
    //componentes de usuario
    private Long colegioId;
    private LoginResponse usuarioLogueado;

    //Datos y estado
    private ObservableList<Curso> modulosData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;

    public ModuloController(TableView<Curso> modulosTable,
                            TableColumn<Curso, Long> colIdModulo,
                            TableColumn<Curso, String> colNombreModulo,
                            Pagination pagination) {
        this.modulosTable = modulosTable;
        this.colIdModulo = colIdModulo;
        this.colNombreModulo = colNombreModulo;
        this.pagination = pagination;

        inicializarTablaModulos();
    }
    public void setUsuarioLogueado(LoginResponse usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        // Manejar colegioId solo si es ADMIN_COLEGIO
        if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO && usuarioLogueado.getColegioId() != null) {
            this.colegioId = usuarioLogueado.getColegioId().longValue();
        }
    }
    public void loadModulos(){
        CompletableFuture<List<Curso>> cargaModulos = CursoService.findByColegioId();
        cargaModulos.thenAccept(modulos ->{
            Platform.runLater(()->{
                modulosData.setAll(modulos);
                modulosTable.setItems(modulosData);
                configurePagination(modulos.size());
            });
        }).exceptionally(e ->{
            Platform.runLater(() -> mostrarError("Error: " + e.getCause().getMessage()));
            return null;
        });
    }
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
        int toIndex = Math.min(fromIndex + itemsPerPage, modulosData.size());
        //crea un observable para la tabla actual
        ObservableList<Curso> pageData = FXCollections.observableArrayList(
                modulosData.subList(fromIndex, toIndex)
        );
        modulosTable.setItems(pageData);
        // refrescar la tabla
        modulosTable.refresh();
    }
    private void inicializarTablaModulos() {
        colIdModulo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreModulo.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        //columna de acciones
        addActionColumn();
    }

    private void addActionColumn() {
        // Eliminar cualquier columna de opciones existente primero
        modulosTable.getColumns().removeIf(col -> "Opciones".equals(col.getText()));
        TableColumn<Curso, Void> colOpciones = new TableColumn<>("Opciones");
        colOpciones.setCellFactory(param -> new TableCell<Curso, Void>() {
            private Button btnOpciones;
            private ContextMenu contextMenu;
            {
                // Inicializar el botón y el menú contextual una vez
                btnOpciones = new Button("⋮");
                contextMenu = new ContextMenu();

                MenuItem verListaItemProfesores = new MenuItem("Ver Profesores");
                MenuItem verListaItemAlumnos = new MenuItem("Ver Alumnos");
                MenuItem editarItem = new MenuItem("Editar");
                MenuItem eliminarItem = new MenuItem("Eliminar");

                contextMenu.getItems().addAll(verListaItemProfesores,verListaItemAlumnos,editarItem, eliminarItem);
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
                if (!empty) {
                    Curso curso = getTableView().getItems().get(getIndex());
                    if (curso != null) {
                        // Configuración normal para otros casos
                        contextMenu.getItems().get(0).setOnAction(event -> verListaProfesores(curso));
                        contextMenu.getItems().get(1).setOnAction(event -> verListaModulo(curso));
                        contextMenu.getItems().get(2).setOnAction(event -> editarModulo(curso));
                        contextMenu.getItems().get(3).setOnAction(event -> eliminarModulo(curso));

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
        modulosTable.getColumns().add(colOpciones);
    }

    //carga la lista de profesores asignados al curso
    private void verListaProfesores(Curso curso) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modulo/profesores_modulo.fxml"));
            Parent root = loader.load();

            ProfesoresModuloController controller = loader.getController();
            controller.initData(curso);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(curso.getNombre());
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //cargar la lsita de alumnos en el curso
    private void verListaModulo(Curso curso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modulo/alumnos_modulo.fxml"));
            Parent root = loader.load();

            AlumnosModuloController controller = loader.getController();
            controller.initData(curso);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(curso.getNombre());
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar la ventana de alumnos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void abrirModalCrearModulo(Stage parentStage){
        try{
            if (usuarioLogueado == null || usuarioLogueado.getRol() != Rol.ADMIN_COLEGIO) {
                CustomAlerts.mostrarError("Solo los administradores de colegio pueden crear módulos");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modulo/crear_modulo.fxml"));
            Parent root = loader.load();

            CrearModuloController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Módulo");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if(controller.isGuardado()){
                String[] datos = controller.getDatosModulo();
                String nombreModulo = datos[0];
                //logica de guardado
                Curso nuevoCurso = new Curso();
                nuevoCurso.setNombre(nombreModulo);

                CursoService.save(nuevoCurso)
                        .thenAccept(cursoGuardado ->{
                            Platform.runLater(()->{
                                CustomAlerts.mostrarExito("Módulo creado exitosamente");
                                loadModulos();
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
    private void eliminarModulo(Curso curso) {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Eliminar Módulo",
                "¿Estás seguro de eliminar este Módulo?\n" +
                        "Se eliminarán también los Alumnos del curso");
        if(confirmacion){
            CursoService.delete(curso.getId())
                    .thenAcceptAsync(v -> Platform.runLater(() ->{
                        modulosData.remove(curso);
                        CustomAlerts.mostrarExito("Módulo eliminado");
                        configurePagination(modulosData.size());
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() ->
                                CustomAlerts.mostrarError("Error: " + ex.getCause().getMessage())
                        );
                        return null;
                    });
        }
    }

    private void editarModulo(Curso curso) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/modulo/editar_modulo.fxml"));
            Parent root = loader.load();

            EditarModuloController controller = loader.getController();
            controller.initData(curso, this::loadModulos);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Módulo");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setStage(stage);
            stage.showAndWait();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el editor");
        }
    }

}
