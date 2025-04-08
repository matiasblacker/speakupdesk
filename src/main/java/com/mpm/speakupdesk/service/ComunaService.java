package com.mpm.speakupdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mpm.speakupdesk.model.Region;
import com.mpm.speakupdesk.model.RegionesWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class ComunaService {
    private ObservableList<Region> regiones = FXCollections.observableArrayList();
    private ObservableList<String> todasLasComunas = FXCollections.observableArrayList();

    public ComunaService() {
        cargarComunasDesdeJSON(); // O desde base de datos
    }
    private void cargarComunasDesdeJSON() {
        try {
            InputStream jsonStream = getClass().getResourceAsStream("/datos/comunas.json");
            ObjectMapper mapper = new ObjectMapper();
            RegionesWrapper wrapper = mapper.readValue(jsonStream, RegionesWrapper.class);

            regiones.setAll(wrapper.getRegiones());
            todasLasComunas.setAll(
                    wrapper.getRegiones().stream()
                            .flatMap(r -> r.getComunas().stream())
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            System.err.println("Error al cargar comunas: " + e.getMessage());
        }
    }
    // Obtener nombres de regiones para el ComboBox
    public ObservableList<String> getNombresRegiones() {
        return regiones.stream()
                .map(Region::getNombre)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    // Filtrar comunas por región y texto
    public ObservableList<String> filtrarComunas(String region, String texto) {
        List<String> comunasDeRegion = regiones.stream()
                .filter(r -> r.getNombre().equals(region))
                .findFirst()
                .map(Region::getComunas)
                .orElse(List.of());

        return comunasDeRegion.stream()
                .filter(comuna -> comuna.toLowerCase().contains(texto.toLowerCase()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
