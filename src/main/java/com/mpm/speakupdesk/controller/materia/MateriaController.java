package com.mpm.speakupdesk.controller.materia;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Materia;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.service.MateriaService;
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

public class MateriaController {

    //componentes de la UI
    private TableView<Materia> materiasTable;
    private TableColumn<Materia, Long> colIdMateria;
    private TableColumn<Materia, String> colNombreMateria;
    private Pagination pagination;
    //componentes de usuario
    private Long colegioId;
    private LoginResponse usuarioLogueado;

    //Datos y estado
    private ObservableList<Materia> materiasData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;

    public MateriaController(TableView<Materia> materiasTable,
                            TableColumn<Materia, Long> colIdMateria,
                            TableColumn<Materia, String> colNombreMateria,
                            Pagination pagination) {
        this.materiasTable = materiasTable;
        this.colIdMateria = colIdMateria;
        this.colNombreMateria = colNombreMateria;
        this.pagination = pagination;

        inicializarTablaMaterias();
    }

    public void setUsuarioLogueado(LoginResponse usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        // Manejar colegioId solo si es ADMIN_COLEGIO
        if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO && usuarioLogueado.getColegioId() != null) {
            this.colegioId = usuarioLogueado.getColegioId().longValue();
        }
    }

    public void loadMaterias(){
        CompletableFuture<List<Materia>> cargaMaterias = MateriaService.findByColegioId();
        cargaMaterias.thenAccept(materias ->{
            Platform.runLater(()->{
                materiasData.setAll(materias);
                materiasTable.setItems(materiasData);
                configurePagination(materias.size());
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
        int toIndex = Math.min(fromIndex + itemsPerPage, materiasData.size());
        //crea un observable para la tabla actual
        ObservableList<Materia> pageData = FXCollections.observableArrayList(
                materiasData.subList(fromIndex, toIndex)
        );
        materiasTable.setItems(pageData);
        // refrescar la tabla
        materiasTable.refresh();
    }

    private void inicializarTablaMaterias() {
        colIdMateria.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreMateria.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        //columna de acciones
        addActionColumn();
    }

    private void addActionColumn() {
        // Eliminar cualquier columna de opciones existente primero
        materiasTable.getColumns().removeIf(col -> "Opciones".equals(col.getText()));
        TableColumn<Materia, Void> colOpciones = new TableColumn<>("Opciones");
        colOpciones.setCellFactory(param -> new TableCell<Materia, Void>() {
            private Button btnOpciones;
            private ContextMenu contextMenu;
            {
                // Inicializar el botón y el menú contextual una vez
                btnOpciones = new Button("⋮");
                contextMenu = new ContextMenu();

                MenuItem verListaItemProfesores = new MenuItem("Ver Profesores");
                MenuItem editarItem = new MenuItem("Editar");
                MenuItem eliminarItem = new MenuItem("Eliminar");

                contextMenu.getItems().addAll(verListaItemProfesores,editarItem, eliminarItem);
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
                    Materia materia = getTableView().getItems().get(getIndex());
                    if (materia != null) {
                        // Configuración normal para otros casos
                        contextMenu.getItems().get(0).setOnAction(event -> verListaProfesores(materia));
                        contextMenu.getItems().get(1).setOnAction(event -> editarModulo(materia));
                        contextMenu.getItems().get(2).setOnAction(event -> eliminarModulo(materia));

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
        materiasTable.getColumns().add(colOpciones);
    }

    private void verListaProfesores(Materia materia) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/materia/profesores_materia.fxml"));
            Parent root = loader.load();

            ProfesoresMateriaController controller = loader.getController();
            controller.initData(materia);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(materia.getNombre());
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void abrirModalCrearMateria(Stage parentStage){
        try{
            if (usuarioLogueado == null || usuarioLogueado.getRol() != Rol.ADMIN_COLEGIO) {
                CustomAlerts.mostrarError("Solo los administradores de colegio pueden crear materias");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/materia/crear_materia.fxml"));
            Parent root = loader.load();

            CrearMateriaController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nueva Materia");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            if(controller.isGuardado()){
                String[] datos = controller.getDatosMateria();
                String nombreMateria = datos[0];
                //logica de guardado
                Materia nuevaMateria = new Materia();
                nuevaMateria.setNombre(nombreMateria);

                MateriaService.save(nuevaMateria)
                        .thenAccept(cursoGuardado ->{
                            Platform.runLater(()->{
                                CustomAlerts.mostrarExito("Materia creada exitosamente");
                                loadMaterias();
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

    private void editarModulo(Materia materia) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/materia/editar_materia.fxml"));
            Parent root = loader.load();

            EditarMateriaController controller = loader.getController();
            controller.initData(materia, this::loadMaterias);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Materia");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            controller.setStage(stage);
            stage.showAndWait();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el editor");
        }

    }

    private void eliminarModulo(Materia materia) {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Eliminar Materia",
                "¿Estás seguro de eliminar esta Materia?\n");
        if(confirmacion){
            MateriaService.delete(materia.getId())
                    .thenAcceptAsync(v -> Platform.runLater(() ->{
                        materiasData.remove(materia);
                        CustomAlerts.mostrarExito("Materia eliminada");
                        configurePagination(materiasData.size());
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() ->
                                CustomAlerts.mostrarError("Error: " + ex.getCause().getMessage())
                        );
                        return null;
                    });
        }
    }

}
