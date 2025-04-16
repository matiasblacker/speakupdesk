package com.mpm.speakupdesk.controller.instituto;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.model.Colegio;
import com.mpm.speakupdesk.service.ColegioService;
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

public class InstitutoController {
    // Componentes de la UI
    private TableView<Colegio> institutosTable;
    private TableColumn<Colegio, Long> colIdInstituto;
    private TableColumn<Colegio, String> colNombreInstituto;
    private TableColumn<Colegio, String> colRegion;
    private TableColumn<Colegio, String> colComuna;
    private Pagination pagination;

    // Datos y estado
    private ObservableList<Colegio> institutosData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;

    public InstitutoController(TableView<Colegio> institutosTable,
                            TableColumn<Colegio, Long> colIdInstituto,
                            TableColumn<Colegio, String> colNombreInstituto,
                            TableColumn<Colegio, String> colRegion,
                            TableColumn<Colegio, String> colComuna,
                            Pagination pagination) {
        this.institutosTable = institutosTable;
        this.colIdInstituto = colIdInstituto;
        this.colNombreInstituto = colNombreInstituto;
        this.colRegion = colRegion;
        this.colComuna = colComuna;
        this.pagination = pagination;

        inicializarTablaInstitutos();
    }

    public void loadInstitutos() {
        CompletableFuture<List<Colegio>> cargaInstitutos = ColegioService.findAll();
        cargaInstitutos.thenAccept(colegios -> {
            Platform.runLater(() -> {
                institutosData.setAll(colegios);
                institutosTable.setItems(institutosData);
                configurePagination(colegios.size());
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarError("Error: " + e.getCause().getMessage()));
            return null;
        });
    }

    private void inicializarTablaInstitutos() {
        colIdInstituto.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreInstituto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colComuna.setCellValueFactory(new PropertyValueFactory<>("comuna"));

        // Añadir columna de acciones
        addActionColumn();
    }

    private void addActionColumn() {
        // Eliminar cualquier columna de opciones existente primero
        institutosTable.getColumns().removeIf(col -> "Opciones".equals(col.getText()));
        TableColumn<Colegio, Void> colOpciones = new TableColumn<>("Opciones");
        colOpciones.setCellFactory(param -> new TableCell<Colegio, Void>() {
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
                if (!empty) {
                    Colegio colegio = getTableView().getItems().get(getIndex());
                    if (colegio != null) {
                        // Configuración normal para otros casos
                        contextMenu.getItems().get(0).setOnAction(event -> editarInstituto(colegio));
                        contextMenu.getItems().get(1).setOnAction(event -> eliminarInstituto(colegio));

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
        institutosTable.getColumns().add(colOpciones);
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
        int toIndex = Math.min(fromIndex + itemsPerPage, institutosData.size());
        //crea un observable para la tabla actual
        ObservableList<Colegio> pageData = FXCollections.observableArrayList(
                institutosData.subList(fromIndex, toIndex)
        );
        institutosTable.setItems(pageData);
        // refrescar la tabla
        institutosTable.refresh();
    }

    public void abrirModalCrearInstituto(Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/instituto/crear_instituto.fxml"));
            Parent root = loader.load();

            CrearInstitutoController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Instituto");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isGuardado()) {
                String[] datos = controller.getDatosInstituto();

                // Lógica de guardado
                Colegio nuevoColegio = new Colegio();
                nuevoColegio.setNombre(datos[0]);
                nuevoColegio.setRegion(datos[1]);
                nuevoColegio.setComuna(datos[2]);

                ColegioService.save(nuevoColegio)
                        .thenAccept(colegioGuardado -> {
                            Platform.runLater(() -> {
                                CustomAlerts.mostrarExito("Institución creada exitosamente");
                                loadInstitutos(); // Recargar la tabla después de guardar
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
        }
    }

    public void editarInstituto(Colegio colegio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/instituto/editar_instituto.fxml"));
            Parent root = loader.load();

            EditarInstitutoController controller = loader.getController();
            controller.initData(colegio, this::loadInstitutos);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Instituto");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();

        } catch (IOException e) {
            CustomAlerts.mostrarError("Error al cargar el editor");
        }
    }

    private void eliminarInstituto(Colegio colegio) {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Eliminar instituto",
                "¿Estás seguro de eliminar este instituto?");
        if (confirmacion) {
            ColegioService.delete(colegio.getId())
                    .thenAcceptAsync(v -> Platform.runLater(() -> {
                        institutosData.remove(colegio);
                        CustomAlerts.mostrarExito("Instituto eliminado");
                        configurePagination(institutosData.size());
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
