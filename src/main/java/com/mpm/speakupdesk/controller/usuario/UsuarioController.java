package com.mpm.speakupdesk.controller.usuario;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.model.Usuario;
import com.mpm.speakupdesk.service.AuthService;
import com.mpm.speakupdesk.service.UsuarioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mpm.speakupdesk.commonutils.CustomAlerts.mostrarError;

public class UsuarioController {
    // Componentes de la UI
    private TableView<Usuario> usuariosTable;
    private TableColumn<Usuario, Long> colId;
    private TableColumn<Usuario, String> colNombre;
    private TableColumn<Usuario, String> colEmail;
    private TableColumn<Usuario, String> colRol;
    private TableColumn<Usuario, String> colEstado;
    private Pagination pagination;

    // Datos y estado
    private ObservableList<Usuario> usuariosData = FXCollections.observableArrayList();
    private int itemsPerPage = 10;
    private int colegioId;
    private LoginResponse usuarioLogueado;

    public UsuarioController(TableView<Usuario> usuariosTable,
                          TableColumn<Usuario, Long> colId,
                          TableColumn<Usuario, String> colNombre,
                          TableColumn<Usuario, String> colEmail,
                          TableColumn<Usuario, String> colRol,
                          TableColumn<Usuario, String> colEstado,
                          Pagination pagination) {
        this.usuariosTable = usuariosTable;
        this.colId = colId;
        this.colNombre = colNombre;
        this.colEmail = colEmail;
        this.colRol = colRol;
        this.colEstado = colEstado;
        this.pagination = pagination;

        inicializarTablaUsuarios();
    }

    public void setUsuarioLogueado(LoginResponse usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado;
        // Manejar colegioId solo si es ADMIN_COLEGIO
        if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO) {
            this.colegioId = usuarioLogueado.getColegioId().intValue();
        }
    }

    public void loadUsuarios() {
        CompletableFuture<List<Usuario>> cargaUsuarios;
        if (usuarioLogueado.getRol() == Rol.ADMIN_GLOBAL) {
            cargaUsuarios = UsuarioService.findAll();
        } else {
            cargaUsuarios = UsuarioService.getUsuariosByColegio(colegioId);
        }
        cargaUsuarios.thenAccept(usuarios -> {
            Platform.runLater(() -> {
                usuariosData.setAll(usuarios);
                configurePagination(usuarios.size());
            });
        }).exceptionally(e -> {
            Platform.runLater(() ->
                    mostrarError("Error: " + e.getCause().getMessage())
            );
            return null;
        });
    }

    // abre el modal del dropdown con la informacion del usuario actual
    public void abrirPerfil() {
        System.out.println("Abrir perfil...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/perfil_usuario.fxml"));
            Parent root = loader.load();

            PerfilUsuarioController controller = loader.getController();
            controller.initData(usuarioLogueado);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Perfil de Usuario");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            mostrarError("Error al cargar la ventana de perfil");
            e.printStackTrace();
        }
    }

    private void inicializarTablaUsuarios() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEstado())
        );
        // Aplicar estilos condicionales
        colEstado.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);  // No mostrar nada si está vacío
                } else {
                    // Crear un StackPane para envolver el texto
                    StackPane stackPane = new StackPane();
                    Label label = new Label(estado);  // Crear el label solo aquí
                    label.setStyle("-fx-text-fill: white;");  // Asegurarse de que el texto sea blanco
                    stackPane.getChildren().add(label);
                    // Aplicar estilos según el estado
                    if (estado.equals("Activo")) {
                        stackPane.getStyleClass().add("estado-activo");
                    } else {
                        stackPane.getStyleClass().add("estado-inactivo");
                    }
                    // Establecer el StackPane como gráfico de la celda
                    setGraphic(stackPane);  // Solo usamos setGraphic aquí
                }
            }
        });
        // Añadir columna de acciones
        addActionColumn();
    }

    private void addActionColumn() {
        // Eliminar cualquier columna de opciones existente primero
        usuariosTable.getColumns().removeIf(col -> "Opciones".equals(col.getText()));
        TableColumn<Usuario, Void> colOpciones = new TableColumn<>("Opciones");
        colOpciones.setCellFactory(param -> new TableCell<Usuario, Void>() {
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
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    if (usuario != null) {
                        // Verificar si es un AdminGlobal intentando editar a otro AdminGlobal
                        boolean esAdminGlobalEditandoOtro = usuarioLogueado.getRol() == Rol.ADMIN_GLOBAL &&
                                usuario.getRol() == Rol.ADMIN_GLOBAL &&
                                !usuarioLogueado.getId().equals(usuario.getId());

                        if (esAdminGlobalEditandoOtro) {
                            // Si es un admin global intentando editar otro admin global, mostrar botón pero con alerta
                            btnOpciones.setOnAction(e ->
                                    CustomAlerts.mostrarError("No puedes editar a otro Admin Global")
                            );
                            setGraphic(btnOpciones);
                        } else {
                            // Configuración normal para otros casos
                            contextMenu.getItems().get(0).setOnAction(event -> editarUsuario(usuario));
                            contextMenu.getItems().get(1).setOnAction(event -> eliminarUsuario(usuario));

                            btnOpciones.setOnAction(e ->
                                    contextMenu.show(btnOpciones, Side.BOTTOM, 0, 0)
                            );
                            setGraphic(btnOpciones);
                        }
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });
        // Añadir la columna al final
        usuariosTable.getColumns().add(colOpciones);
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
        int toIndex = Math.min(fromIndex + itemsPerPage, usuariosData.size());
        //crea un observable para la tabla actual
        ObservableList<Usuario> pageData = FXCollections.observableArrayList(
                usuariosData.subList(fromIndex, toIndex)
        );
        usuariosTable.setItems(pageData);
        // refrescar la tabla
        usuariosTable.refresh();
    }

    public void abrirModalCrearUsuario(Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/crear_usuario.fxml"));
            Parent root = loader.load();

            CrearUsuarioController controller = loader.getController();
            controller.initData(usuarioLogueado, this::loadUsuarios);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Crear Nuevo Usuario");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait(); // Bloquea la ventana principal hasta cerrar el modal

        } catch (IOException e) {
            mostrarError("Error al abrir el formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void editarUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/usuario/editar_usuario.fxml"));
            Parent root = loader.load();

            EditarUsuarioController controller = loader.getController();
            controller.initData(usuario, this::loadUsuarios);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Editar Usuario");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            controller.setStage(stage);
            stage.showAndWait();
        } catch (IOException e) {
            mostrarError("Error al cargar el editor de usuarios");
            e.printStackTrace();
        }
    }

    private void eliminarUsuario(Usuario usuario) {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Eliminar usuario", "¿Estás seguro de que quieres eliminar este usuario?");
        if (confirmacion) {
            try {
                //llamada al endpoint de eliminacion de usuarios
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://localhost:8080/api/usuarios/eliminar/" + usuario.getId())
                        .delete()
                        .addHeader("Authorization", "Bearer " + AuthService.getToken())
                        .build();
                //respuesta exitosa
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        Platform.runLater(() -> {
                            usuariosData.remove(usuario);
                            // Mostrar alerta de éxito en pantalla
                            CustomAlerts.mostrarExito("Usuario eliminado exitosamente.");
                            configurePagination(usuariosData.size());
                        });
                    }else {
                        // Si la respuesta no es exitosa, mostrar error en pantalla
                        mostrarError("No se pudo eliminar el usuario. Error: " + response.message());
                    }
                }
            } catch (IOException e) {
                System.out.println( "Error al eliminar" + e.getMessage());
            }
        }else {
            mostrarError("La eliminación ha sido cancelada.");
        }
    }
}
