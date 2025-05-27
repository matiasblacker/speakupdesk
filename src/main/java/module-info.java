module com.mpm.speakupdesk {
    requires com.fasterxml.jackson.databind;
    requires javafx.controls;
    requires javafx.fxml;
    requires okhttp3;
    requires de.jensd.fx.glyphs.fontawesome;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires javafx.graphics;
    requires org.controlsfx.controls;
    requires java.prefs;
    requires java.desktop;
    requires java.logging;

    opens com.mpm.speakupdesk to javafx.fxml;
    // Abre el paquete del controlador para que FXML pueda acceder a los métodos anotados con @FXML
    opens com.mpm.speakupdesk.controller to javafx.fxml;
    opens com.mpm.speakupdesk.dto.request to com.fasterxml.jackson.databind;
    opens com.mpm.speakupdesk.dto.response to com.fasterxml.jackson.databind;
    opens com.mpm.speakupdesk.model to javafx.base, com.fasterxml.jackson.databind; // Para deserialización JSON

    exports com.mpm.speakupdesk;
    // Exporta el paquete del controlador para que FXMLLoader pueda acceder a él
    exports com.mpm.speakupdesk.controller to javafx.fxml;
    exports com.mpm.speakupdesk.controller.instituto to javafx.fxml;
    opens com.mpm.speakupdesk.controller.instituto to javafx.fxml;
    exports com.mpm.speakupdesk.controller.usuario to javafx.fxml;
    opens com.mpm.speakupdesk.controller.usuario to javafx.fxml;
    exports com.mpm.speakupdesk.controller.modulo to javafx.fxml;
    opens com.mpm.speakupdesk.controller.modulo to javafx.fxml;
    exports com.mpm.speakupdesk.controller.alumno to javafx.fxml;
    opens com.mpm.speakupdesk.controller.alumno to javafx.fxml;
    exports com.mpm.speakupdesk.controller.materia to javafx.fxml;
    opens com.mpm.speakupdesk.controller.materia to javafx.fxml;
}