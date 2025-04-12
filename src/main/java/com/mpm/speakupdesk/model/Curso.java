package com.mpm.speakupdesk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Curso {
    private Long id;
    private String nombre;
    private Long colegioId;

    public Curso() {
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
}
