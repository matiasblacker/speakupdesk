package com.mpm.speakupdesk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Materia {
    private Long id;
    private String nombre;
    private Long colegioId;

    public Materia() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getColegioId() {
        return colegioId;
    }

    public void setColegioId(Long colegioId) {
        this.colegioId = colegioId;
    }

    @Override
    public String toString() {
        return this.nombre;
    }
}
