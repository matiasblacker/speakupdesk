package com.mpm.speakupdesk.controller;

import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Colegio;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.model.Usuario;
import com.mpm.speakupdesk.service.AuthService;
import com.mpm.speakupdesk.service.ColegioService;
import com.mpm.speakupdesk.service.UsuarioService;
import com.mpm.speakupdesk.commonutils.CustomAlerts;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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

public class AdminController {
    // Componentes de la UI top bar
    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private Button btnPerfil;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;
    @FXML private Button btnCrearUsuario;
    @FXML private Button btnCrearInstituto;
    @FXML private Button btnCrearModulo;
    @FXML private Button btnCrearAlumnos;
    //tabpane
    @FXML private TabPane tabPane;
    // Tabla de usuarios
    @FXML private TableView<Usuario> usuariosTable;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;
    @FXML private Pagination pagination;
    //tabla institutos
    @FXML private TableView<Colegio> institutosTable;
    @FXML private TableColumn<Colegio, Long> colIdInstituto;
    @FXML private TableColumn<Colegio, String> colNombreInstituto;
    @FXML private TableColumn<Colegio, String> colRegion;
    @FXML private TableColumn<Colegio, String> colComuna;

    // para ocultar botones y elementos de acuerdo al rol
    private static final PseudoClass HIDDEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("hidden");

    // Datos y estado
    private ObservableList<Usuario> usuariosData = FXCollections.observableArrayList();
    private ObservableList<Colegio> institutosData = FXCollections.observableArrayList();

    private int itemsPerPage = 10;
    private int colegioId;
    private Stage stage;
    private ContextMenu contextMenu;
    //obtener datos del usuario para cargar al modal de perfil
    private LoginResponse usuarioLogueado;

    //inicializacion de componentes
    public void initialize() {
        //menu de usuario, perfil y cerrar sesion
        setupContextMenu();
        //configura la tabla usuarios
        inicializarTablaUsuarios();
        // configura la tabla institutos
        inicializarTablaInstitutos();

        //carga la alertas
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().equals("Usuarios")) {
                loadUsuarios();
            } else if (newTab.getText().equals("Institutos")) {
                loadInstitutos();
            }
        });
    }
    //configuracion de la escena
    private void configureStage() {
        this.stage = (Stage) lblNombre.getScene().getWindow();
        if (stage != null) {
            stage.setMaximized(true);
        }
    }

    //A partir de aqui comenzamos con los componentes de usuario
    //Es responsable de inicializar el AdminController con los datos del usuario que ha iniciado sesión
    public void initData(LoginResponse usuario) {
        this.usuarioLogueado = usuario;

        // Manejar colegioId solo si es ADMIN_COLEGIO
        if (usuario.getRol() == Rol.ADMIN_COLEGIO) {
            this.colegioId = usuario.getColegioId().intValue();
        }
        Platform.runLater(() -> {
            if (usuario != null) {
                lblNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
                lblRol.setText("Rol: " + usuario.getRol());
                configurarUi(); // muestra u oculta componentes según el rol
                loadUsuarios();
            }
            configureStage();
        });
    }
    //carga los usuarios del mismo colegio del admin
    private void loadUsuarios() {
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
    //carga los institutos
    private void loadInstitutos() {
        CompletableFuture<List<Colegio>> cargaInstitutos = ColegioService.findAll();
        cargaInstitutos.thenAccept(colegios -> {
            Platform.runLater(() -> {
                institutosData.setAll(colegios);
                institutosTable.setItems(institutosData); // ¡Falta esta línea!
                configurePagination(colegios.size());
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> mostrarError("Error: " + e.getCause().getMessage()));
            return null;
        });
    }

    //menu dropdown de usuario con perfil y cerrar sesion
    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem perfilItem = new MenuItem("Perfil");
        MenuItem cerrarSesionItem = new MenuItem("Cerrar sesión");

        contextMenu.getItems().addAll(perfilItem, cerrarSesionItem);
        perfilItem.setOnAction(e -> abrirPerfil());
        cerrarSesionItem.setOnAction(e -> cerrarSesion());

        btnPerfil.setOnMouseClicked(e -> contextMenu.show(btnPerfil, e.getScreenX(), e.getScreenY()));
    }
    // abre el modal del dropdown con la informacion del usuario actual
    private void abrirPerfil() {
        System.out.println("Abrir perfil...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/perfil_usuario.fxml"));
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
    //cierra la sesion y la aplicación
    private void cerrarSesion() {
        boolean confirmacion = CustomAlerts.mostrarConfirmacion("Cerrar sesion", "¿Seguro que quieres cerrar la sesión?");
        if (confirmacion){
            System.out.println("Cerrando sesión...");
            Platform.runLater(() -> {
                // Cerrar la ventana actual
                if (stage != null) {
                    stage.close();
                }
            });
        }
    }
    //A partir de aqui toda la configuracion de la tabla de usuarios
    //configura los parametros de las columnas de la tabla
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
        //añade la columa de opciones
        addActionColumn();
    }
    private void inicializarTablaInstitutos() {
        colIdInstituto.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreInstituto.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRegion.setCellValueFactory(new PropertyValueFactory<>("region"));
        colComuna.setCellValueFactory(new PropertyValueFactory<>("comuna"));
        //añade la columa de opciones
        addActionColumn();
    }

    //Añade la columa de opciones al final de la tabla
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
    //configuracion del paginador de la la tabla
    private void configurePagination(int totalItems) {
        int pageCount = (totalItems + itemsPerPage - 1) / itemsPerPage;
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount); // Evitar 0 páginas
        pagination.setCurrentPageIndex(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                updateTablePage(newIndex.intValue())
        );
        updateTablePage(0);
    }
    //actualiza las paginas de la tabla
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

    //abre un modal con los datos del usuario a editar
    private void editarUsuario(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/editar_usuario.fxml"));
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
    // Elimina un usuario de los registros
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
    //actualiza las tablas cuando se da click en actualizar
    public void ActualizarTablas(ActionEvent actionEvent) {
        //recarga los usuarios
        loadUsuarios();
        //recarga los cursos
        //recarla los alumnos
        System.out.println("tablas actualizadas");
        CustomAlerts.mostrarExito("Tablas actualizadas");
    }
    //Crear Usuarios
    @FXML
    private void abrirModalCrearUsuario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/crear_usuario.fxml"));
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
    //configuracion de la UI
    // actualizar el estado del boton
    private void updateButtonVisibility(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible); // Opcional: Libera espacio si es false
        button.pseudoClassStateChanged(HIDDEN_PSEUDO_CLASS, !visible);
    }
    // muestra u oculta componentes según el rol
    private void configurarUi() {
        if (usuarioLogueado.getRol() == Rol.ADMIN_GLOBAL) {
            //botones sidebar
            updateButtonVisibility(btnCrearInstituto, true);
            updateButtonVisibility(btnCrearModulo, false);
            updateButtonVisibility(btnCrearAlumnos, false);
        } else if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO) {
            //botones sidebar
            updateButtonVisibility(btnCrearModulo, true);
            updateButtonVisibility(btnCrearAlumnos,true);
            updateButtonVisibility(btnCrearInstituto, false);
        } else {
            updateButtonVisibility(btnCrearInstituto,false);
            updateButtonVisibility(btnCrearModulo,false);
            updateButtonVisibility(btnCrearAlumnos, false);
            updateButtonVisibility(btnCrearUsuario, false);
        }
    }

    // AdminController.java
    // En AdminController.java
    @FXML
    private void abrirModalCrearInstituto() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/crear_instituto.fxml"));
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
                        .thenAccept(colegioGuardado -> Platform.runLater(() ->
                                CustomAlerts.mostrarExito("Institución creada exitosamente")
                        ))
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
}