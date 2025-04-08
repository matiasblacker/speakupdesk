package com.mpm.speakupdesk.model;

import java.util.List;

public class Region {
    private String nombre;
    private List<String> comunas;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getComunas() {
        return comunas;
    }

    public void setComunas(List<String> comunas) {
        this.comunas = comunas;
    }
}
