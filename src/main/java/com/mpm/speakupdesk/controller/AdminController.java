package com.mpm.speakupdesk.controller;

import com.mpm.speakupdesk.controller.instituto.InstitutoController;
import com.mpm.speakupdesk.controller.modulo.ModuloController;
import com.mpm.speakupdesk.controller.usuario.UsuarioController;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.Colegio;
import com.mpm.speakupdesk.model.Curso;
import com.mpm.speakupdesk.model.Rol;
import com.mpm.speakupdesk.model.Usuario;
import com.mpm.speakupdesk.commonutils.CustomAlerts;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AdminController {
    // Componentes de la UI top bar
    @FXML private Label lblNombre;
    @FXML private Label lblRol;
    @FXML private Button btnPerfil;
    @FXML private HBox alertContainer;
    @FXML private Label alertLabel;

    //tabpane
    @FXML private TabPane tabPane;
    @FXML private Tab tabUsuarios;
    @FXML private Tab tabInstitutos;
    @FXML private Tab tabModulos;
    @FXML private Tab tabAlumnos;
    @FXML private Button btnCrearUsuario;
    @FXML private Button btnCrearInstituto;
    @FXML private Button btnCrearModulo;
    @FXML private Button btnCrearAlumnos;

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
    @FXML private Pagination paginationInstitutos;
    //tabla modulos
    @FXML private TableView<Curso> modulosTable;
    @FXML private TableColumn<Curso, Long> colIdModulo;
    @FXML private TableColumn<Curso, String> colNombreModulo;
    @FXML private Pagination paginationModulo;

    // para ocultar botones y elementos de acuerdo al rol
    private static final PseudoClass HIDDEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("hidden");
    // Datos y estado
    private Stage stage;
    private ContextMenu contextMenu;
    //obtener datos del usuario para cargar al modal de perfil
    private LoginResponse usuarioLogueado;
    // Controladores
    private UsuarioController usuarioController;
    private InstitutoController institutoController;
    private ModuloController moduloController;
    //inicializacion de componentes
    public void initialize() {
        //menu de usuario, perfil y cerrar sesion
        setupContextMenu();
        // Inicializar controladores
        usuarioController = new UsuarioController(usuariosTable, colId, colNombre, colEmail, colRol, colEstado, pagination);
        institutoController = new InstitutoController(institutosTable, colIdInstituto, colNombreInstituto, colRegion, colComuna, paginationInstitutos);
        moduloController = new ModuloController(modulosTable,colIdModulo,colNombreModulo,paginationModulo);
        //carga la alertas
        CustomAlerts.setAlertComponents(alertContainer, alertLabel);
        // Listener para cambio de tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab.getText().equals("Usuarios")) {
                usuarioController.loadUsuarios();
            } else if (newTab.getText().equals("Institutos")) {
                institutoController.loadInstitutos();
            } else if (newTab.getText().equals("Módulos")) {
                moduloController.loadModulos();
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
    //Es responsable de inicializar el AdminController con los datos del usuario que ha iniciado sesión
    public void initData(LoginResponse usuario) {
        this.usuarioLogueado = usuario;
        // Configurar managers con la información del usuario
        usuarioController.setUsuarioLogueado(usuario);
        Platform.runLater(() -> {
            if (usuario != null) {
                lblNombre.setText(usuario.getNombre() + " " + usuario.getApellido());
                lblRol.setText("Rol: " + usuario.getRol());
                configurarUi(); // muestra u oculta componentes según el rol
                usuarioController.loadUsuarios();
            }
            configureStage();
        });
    }

    //menu dropdown de usuario con perfil y cerrar sesion
    private void setupContextMenu() {
        contextMenu = new ContextMenu();
        MenuItem perfilItem = new MenuItem("Perfil");
        MenuItem cerrarSesionItem = new MenuItem("Cerrar sesión");

        contextMenu.getItems().addAll(perfilItem, cerrarSesionItem);
        perfilItem.setOnAction(e -> usuarioController.abrirPerfil());
        cerrarSesionItem.setOnAction(e -> cerrarSesion());

        btnPerfil.setOnMouseClicked(e -> contextMenu.show(btnPerfil, e.getScreenX(), e.getScreenY()));
    }
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
    @FXML
    public void abrirModalCrearInstituto(ActionEvent actionEvent) {
        institutoController.abrirModalCrearInstituto(stage);
    }
    @FXML
    public void abrirModalCrearUsuario(ActionEvent actionEvent) {
        usuarioController.abrirModalCrearUsuario(stage);
    }
    @FXML
    public void abrirModalCrearModulo(ActionEvent actionEvent) {
        moduloController.setUsuarioLogueado(usuarioLogueado);
        moduloController.abrirModalCrearModulo(stage);
    }

    public void ActualizarTablas(ActionEvent actionEvent) {
        if (usuarioLogueado.getRol() == Rol.ADMIN_GLOBAL){
            usuarioController.loadUsuarios();
            institutoController.loadInstitutos();
        } else if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO) {
            usuarioController.loadUsuarios();
            moduloController.loadModulos();
        }
        System.out.println("tablas actualizadas");
        CustomAlerts.mostrarExito("Tablas actualizadas");
    }

    // muestra u oculta componentes según el rol
    private void configurarUi() {
        if (usuarioLogueado.getRol() == Rol.ADMIN_GLOBAL) {
            //botones sidebar
            updateButtonVisibility(btnCrearInstituto,tabInstitutos,tabPane, true);
            updateButtonVisibility(btnCrearModulo,tabModulos,tabPane, false);
            updateButtonVisibility(btnCrearAlumnos,tabAlumnos,tabPane, false);
        } else if (usuarioLogueado.getRol() == Rol.ADMIN_COLEGIO) {
            //botones sidebar
            updateButtonVisibility(btnCrearModulo,tabModulos,tabPane, true);
            updateButtonVisibility(btnCrearAlumnos,tabAlumnos,tabPane,true);
            updateButtonVisibility(btnCrearInstituto,tabInstitutos,tabPane,false);
        } else {
            updateButtonVisibility(btnCrearInstituto,tabInstitutos,tabPane,false);
            updateButtonVisibility(btnCrearModulo,tabModulos,tabPane,false);
            updateButtonVisibility(btnCrearAlumnos,tabAlumnos,tabPane, false);
            updateButtonVisibility(btnCrearUsuario,tabUsuarios,tabPane, false);
        }
    }
    private void updateButtonVisibility(Button button,Tab tab, TabPane tabPane, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible); // Opcional: Libera espacio si es false
        button.pseudoClassStateChanged(HIDDEN_PSEUDO_CLASS, !visible);

        if (visible && !tabPane.getTabs().contains(tab)) {
            tabPane.getTabs().add(tab);
        } else if (!visible) {
            tabPane.getTabs().remove(tab);
        }
    }
}