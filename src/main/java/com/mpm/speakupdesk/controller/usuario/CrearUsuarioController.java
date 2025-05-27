package com.mpm.speakupdesk.controller.usuario;

import com.mpm.speakupdesk.commonutils.CustomAlerts;
import com.mpm.speakupdesk.dto.response.LoginResponse;
import com.mpm.speakupdesk.model.*;
import com.mpm.speakupdesk.service.ColegioService;
import com.mpm.speakupdesk.service.CursoService;
import com.mpm.speakupdesk.service.MateriaService;
import com.mpm.speakupdesk.service.UsuarioService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckListView;

import java.util.List;
import java.util.stream.Collectors;

public class CrearUsuarioController {
    // Componentes UI
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<Rol> cbRol;
    @FXML private ComboBox<Colegio> cbColegio;
    @FXML private VBox colegioContainer; // Contenedor para el combo de colegio

    @FXML private HBox Container;
    @FXML private CheckListView<Curso> clvCursos;  // Cambiado de ListView a CheckListView
    @FXML private CheckListView<Materia> clvMaterias;  // Cambiado de ListView a CheckListView

    private Stage stage;
    private Runnable onSuccess;
    private LoginResponse usuarioLogueado;
    private boolean mostrarCampoColegio = false;
    private boolean mostrarCampoCursos = false;
    private boolean mostrarCampoMaterias = false;

    private ObservableList<Curso> cursosDisponibles = FXCollections.observableArrayList();
    private ObservableList<Materia> materiasDisponibles = FXCollections.observableArrayList();

    @FXML public void initialize() {
        configurarComboBox();
        configurarCheckListViews(); // Nuevo método para configurar CheckListViews
    }

    private void configurarCheckListViews() {

    }

    public void initData(LoginResponse usuario, Runnable onSuccess) {
        this.usuarioLogueado = usuario;
        this.onSuccess = onSuccess;
        // Configurar los roles disponibles según el usuario logueado
        configurarRolesDisponibles();
        // Configurar visibilidad del campo colegio y contraseña
        configurarCampos();
    }

    private void configurarRolesDisponibles() {
        // Según el rol del usuario logueado, configuramos los roles que puede crear
        if (Rol.valueOf(String.valueOf(usuarioLogueado.getRol())) == Rol.ADMIN_COLEGIO) {
            // Admin colegio solo puede crear profesores
            cbRol.setItems(FXCollections.observableArrayList(Rol.PROFESOR));
            cbRol.setValue(Rol.PROFESOR);
            cbRol.setDisable(true); // Solo hay una opción, así que lo deshabilitamos

            // Si es admin_colegio y va a crear profesor, mostrar cursos directamente
            mostrarCampoCursos = true;
            mostrarCampoMaterias = true;
            if(Container != null) {
                Container.setVisible(true);
                Container.setManaged(true);
                cargarCursosColegio();
                cargarMateriasColegio();
            }
        } else if (Rol.valueOf(String.valueOf(usuarioLogueado.getRol())) == Rol.ADMIN_GLOBAL) {
            // Admin global puede crear admin global y admin colegio
            cbRol.setItems(FXCollections.observableArrayList(Rol.ADMIN_GLOBAL, Rol.ADMIN_COLEGIO));
            cbRol.setValue(Rol.ADMIN_GLOBAL);
        }
    }

    private void configurarCampos() {
        // Para los profesores y admin_colegio, la contraseña se genera automáticamente
        cbRol.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Mostrar/ocultar el selector de colegio
            boolean mostrarColegio = (newVal == Rol.ADMIN_COLEGIO);
            mostrarCampoColegio = mostrarColegio;
            if (colegioContainer != null) {
                colegioContainer.setVisible(mostrarColegio);
                colegioContainer.setManaged(mostrarColegio);
            }

            // Mostrar/ocultar selector de cursos solo para PROFESOR
            boolean mostrarCursos = (newVal == Rol.PROFESOR);
            boolean mostrarMaterias = (newVal == Rol.PROFESOR);
            mostrarCampoCursos = mostrarCursos;
            mostrarCampoMaterias = mostrarMaterias;
            if (Container != null) {
                Container.setVisible(mostrarCursos);
                Container.setManaged(mostrarCursos);
                Container.setVisible(mostrarMaterias);
                Container.setManaged(mostrarMaterias);

                // Cargar cursos si es necesario y es un PROFESOR
                if (mostrarCursos &&
                        Rol.valueOf(String.valueOf(usuarioLogueado.getRol())) == Rol.ADMIN_COLEGIO) {
                    cargarCursosColegio();
                }
            }
        });
        // Configuración inicial
        boolean mostrarColegio = (cbRol.getValue() == Rol.ADMIN_COLEGIO);
        mostrarCampoColegio = mostrarColegio;
        if (colegioContainer != null) {
            colegioContainer.setVisible(mostrarColegio);
            colegioContainer.setManaged(mostrarColegio);

            // Cargar colegios si es necesario
            if (mostrarColegio) {
                cargarColegios();
            }
        }
        // Configuración inicial para cursos
        boolean mostrarCursos = (cbRol.getValue() == Rol.PROFESOR);
        mostrarCampoCursos = mostrarCursos;

        boolean mostrarMaterias = (cbRol.getValue() == Rol.PROFESOR);
        mostrarCampoMaterias = mostrarMaterias;

        if(Container != null){
            Container.setVisible(mostrarCursos);
            Container.setManaged(mostrarCursos);
            Container.setVisible(mostrarMaterias);
            Container.setManaged(mostrarMaterias);

            // Cargar los cursos si es PROFESOR y el usuario logueado es ADMIN_COLEGIO
            if(mostrarCursos && mostrarMaterias &&
                    Rol.valueOf(String.valueOf(usuarioLogueado.getRol())) == Rol.ADMIN_COLEGIO) {
                cargarCursosColegio();
            }
        }
    }

    private void cargarColegios() {
        ColegioService.findAll()
                .thenAccept(colegios -> {
                    Platform.runLater(() -> {
                        cbColegio.setItems(FXCollections.observableArrayList(colegios));
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al cargar los colegios: " + e.getMessage())
                    );
                    return null;
                });
    }

    private void cargarCursosColegio(){
        CursoService.findByColegioId()
                .thenAccept(cursos -> {
                    System.out.println("cursos cargados: " + cursos.size());
                    cursosDisponibles.setAll(cursos);
                    Platform.runLater(() -> {
                        clvCursos.setItems(cursosDisponibles); // Actualizado para CheckListView
                    });
                })
                .exceptionally(e ->{
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al cargar los cursos: " + e.getMessage())
                    );
                    return null;
                });
    }

    private void cargarMateriasColegio() {
        MateriaService.findByColegioId()
                .thenAccept(materias -> {
                    System.out.println("materias cargadas: " + materias.size());
                    materiasDisponibles.setAll(materias);
                    Platform.runLater(() -> {
                        clvMaterias.setItems(materiasDisponibles); // Actualizado para CheckListView
                    });
                })
                .exceptionally(e ->{
                    Platform.runLater(() ->
                            CustomAlerts.mostrarError("Error al cargar las materias: " + e.getMessage())
                    );
                    return null;
                });
    }

    private void configurarComboBox() {
        // Configurar ComboBox de Rol
        cbRol.setCellFactory(param -> new ListCell<Rol>() {
            @Override
            protected void updateItem(Rol rol, boolean empty) {
                super.updateItem(rol, empty);
                setText(empty ? null : rol.toString());
            }
        });
        cbRol.setButtonCell(new ListCell<Rol>() {
            @Override
            protected void updateItem(Rol rol, boolean empty) {
                super.updateItem(rol, empty);
                setText(empty ? null : rol.toString());
            }
        });
        // Configurar ComboBox de Colegio
        cbColegio.setCellFactory(param -> new ListCell<Colegio>() {
            @Override
            protected void updateItem(Colegio colegio, boolean empty) {
                super.updateItem(colegio, empty);
                setText(empty ? null : colegio.getNombre());
            }
        });
        cbColegio.setButtonCell(new ListCell<Colegio>() {
            @Override
            protected void updateItem(Colegio colegio, boolean empty) {
                super.updateItem(colegio, empty);
                setText(empty ? null : colegio.getNombre());
            }
        });
    }

    @FXML
    private void crearUsuario() {
        if (!validarCampos()) return;
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(txtNombre.getText().trim());
        nuevoUsuario.setApellido(txtApellido.getText().trim());
        nuevoUsuario.setEmail(txtEmail.getText().trim());
        nuevoUsuario.setRol(cbRol.getValue().name());

        if (mostrarCampoColegio && cbColegio.getValue() != null) {
            nuevoUsuario.setColegioId(cbColegio.getValue().getId());
        }

        // Obtener los cursos seleccionados usando CheckListView
        List<Long> cursosSeleccionadosIds = null;
        if (mostrarCampoCursos && !clvCursos.getCheckModel().getCheckedItems().isEmpty()) {
            cursosSeleccionadosIds = clvCursos.getCheckModel().getCheckedItems().stream()
                    .map(Curso::getId)
                    .collect(Collectors.toList());
        }

        // Obtener las materias seleccionadas usando CheckListView
        List<Long> materiasSeleccionadasIds = null;
        if(mostrarCampoMaterias && !clvMaterias.getCheckModel().getCheckedItems().isEmpty()){
            materiasSeleccionadasIds = clvMaterias.getCheckModel().getCheckedItems().stream()
                    .map(Materia::getId)
                    .collect(Collectors.toList());
        }

        UsuarioService.crearUsuario(nuevoUsuario, cursosSeleccionadosIds, materiasSeleccionadasIds)
                .thenAccept(created -> Platform.runLater(() -> {
                    CustomAlerts.mostrarExito("Usuario creado exitosamente. Se envió una contraseña temporal al correo.");
                    if (onSuccess != null) onSuccess.run();
                    cerrarModal();
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        // Verificar si es un error de email duplicado
                        if (errorMsg.contains("correo electrónico ya está registrado") ||
                                errorMsg.contains("email ya está registrado")) {
                            CustomAlerts.mostrarAdvertencia("El email ingresado ya existe en el sistema. Por favor, utilice otro.");
                        } else {
                            CustomAlerts.mostrarError("Error: " + errorMsg);
                        }
                    });
                    return null;
                });
    }

    private boolean validarCampos() {
        if (txtNombre.getText().isBlank() ||
                txtApellido.getText().isBlank() ||
                txtEmail.getText().isBlank()) {
            CustomAlerts.mostrarAdvertencia("Todos los campos son obligatorios");
            return false;
        }
        if (!txtEmail.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            CustomAlerts.mostrarAdvertencia("Formato de email inválido");
            return false;
        }
        return true;
    }

    @FXML
    private void cancelar() {
        cerrarModal();
    }

    private void cerrarModal() {
        if (stage != null) {
            stage.close();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}