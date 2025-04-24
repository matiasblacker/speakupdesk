package com.mpm.speakupdesk.model;

public class Alumno {
    private Long id;
    private String nombre;
    private String apellido;
    private Integer numeroLista;
    private Long cursoId;
    private String nombreCurso;

    public Alumno() {
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getNumeroLista() {
        return numeroLista;
    }

    public void setNumeroLista(Integer numeroLista) {
        this.numeroLista = numeroLista;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }


}
